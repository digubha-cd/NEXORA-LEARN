package com.example.ui.screens.friends

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.AppConstants
import com.example.core.model.Friend
import com.example.core.model.PublicStudentProfile
import com.example.core.model.Subject
import com.example.core.repository.SyllabusRepository
import com.example.ui.components.NexoraCard
import com.example.ui.components.NexoraTargetBadge
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraPink
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraSuccess
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary

/**
 * Full-Screen Public Study Profile Screen for NEXORA LEARN.
 *
 * Requirements:
 * Shows complete public study profile:
 * - Name (with (You) if current student)
 * - Avatar with initials, background color, online status
 * - 90+ target badge
 * - Overall completed chapters / total chapters
 * - Overall study %
 * - Total Chapter Points
 * - All 7 subjects (Gujarati, English, SP & CC, B.A., Statistics, Elements of Accounts, Economics)
 * - Each subject completed / total chapters
 * - Each subject %
 * - Each subject Chapter Points
 * - Completed chapter names (with interactive expandable view)
 * - Pending chapter names (with interactive expandable view)
 * - Completed To-Do count
 * - Study streak
 * - Competition points
 * - Leaderboard rank
 *
 * Privacy Guarantees:
 * - Mobile number, Gmail/email, password, login credentials, and private account data
 *   are strictly protected and NEVER exposed.
 */
@Composable
fun FriendProfileScreen(
    profile: PublicStudentProfile,
    onBackClick: () -> Unit,
    onOpenChat: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val completedIds = profile.completedChapterIds
    val totalChapters = remember(completedIds) { SyllabusRepository.getTotalChaptersCount() }
    val totalCompleted = remember(completedIds) { SyllabusRepository.getTotalCompletedChaptersCount(completedIds) }
    val totalPending = remember(completedIds) { SyllabusRepository.getTotalPendingChaptersCount(completedIds) }
    val overallProgress = remember(completedIds) { SyllabusRepository.getOverallProgress(completedIds) }
    val overallPercentInt = (overallProgress * 100).toInt()
    val totalEarnedPoints = remember(completedIds) { SyllabusRepository.getTotalEarnedPoints(completedIds) }
    val maxPoints = remember(completedIds) { SyllabusRepository.getMaxPossiblePoints() }

    // State to track expanded subjects for completed/pending chapter names
    val expandedSubjectMap = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("friend_profile_full_screen"),
        containerColor = NexoraBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NexoraSurface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("friend_profile_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NexoraTextPrimary
                            )
                        }

                        Column {
                            Text(
                                text = if (profile.isCurrentStudent) "Your Public Profile" else "Student Profile",
                                color = NexoraTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "GSEB Class 12 Commerce",
                                color = NexoraCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Message Action Button in TopBar (only for friends/peers)
                    if (onOpenChat != null && !profile.isCurrentStudent) {
                        Button(
                            onClick = onOpenChat,
                            colors = ButtonDefaults.buttonColors(containerColor = NexoraCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("friend_profile_top_chat_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Chat,
                                contentDescription = null,
                                tint = NexoraBackground,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Message",
                                color = NexoraBackground,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Hero Profile Card
                NexoraCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = NexoraSurface,
                    borderColor = if (profile.isCurrentStudent) NexoraCyan.copy(alpha = 0.6f) else NexoraBorder,
                    contentPadding = 20.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar with Status Ring
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(Color(profile.avatarColorHex))
                                    .border(2.dp, NexoraCyan.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.avatarInitials,
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (profile.isOnline || profile.isCurrentStudent) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(NexoraSuccess, CircleShape)
                                        .border(3.dp, NexoraSurface, CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (profile.isCurrentStudent) "${profile.studentName} (You)" else profile.studentName,
                            color = NexoraTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${profile.statusText} • Gujarati Medium",
                            color = NexoraTextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Target and Leaderboard Rank Badges
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NexoraTargetBadge(
                                targetTitle = profile.targetMarks,
                                compact = true
                            )

                            if (profile.leaderboardRank > 0) {
                                Box(
                                    modifier = Modifier
                                        .background(NexoraGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .border(1.dp, NexoraGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.EmojiEvents,
                                            contentDescription = null,
                                            tint = NexoraGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = when (profile.leaderboardRank) {
                                                1 -> "🥇 1st on Leaderboard"
                                                2 -> "🥈 2nd on Leaderboard"
                                                3 -> "🥉 3rd on Leaderboard"
                                                else -> "Rank #${profile.leaderboardRank}"
                                            },
                                            color = NexoraGold,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Objective Activity Stats Row (To-Dos, Streak, Points)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BigStatCard(
                        title = "Completed To-Dos",
                        value = "${profile.completedTodoCount}",
                        icon = Icons.Outlined.CheckCircle,
                        tint = NexoraCyan,
                        modifier = Modifier.weight(1f)
                    )
                    BigStatCard(
                        title = "Study Streak",
                        value = "${profile.streakDays}d",
                        icon = Icons.Filled.Whatshot,
                        tint = NexoraGold,
                        modifier = Modifier.weight(1f)
                    )
                    BigStatCard(
                        title = "Competition Pts",
                        value = "${profile.competitionPoints}",
                        icon = Icons.Filled.EmojiEvents,
                        tint = NexoraPink,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- OVERALL STUDY PROGRESS CARD ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MenuBook,
                                contentDescription = null,
                                tint = NexoraCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Study Progress",
                                color = NexoraTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "$overallPercentInt% Completed",
                            color = NexoraCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    NexoraCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 16.dp
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Overall Progress Bar
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Overall Syllabus Coverage",
                                        color = NexoraTextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "$totalCompleted / $totalChapters Chapters",
                                        color = NexoraTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { overallProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = NexoraCyan,
                                    trackColor = NexoraSurfaceElevated,
                                    strokeCap = StrokeCap.Round
                                )
                            }

                            // 3 KPIs: Completed Chapters, Pending Chapters, Total Chapter Points
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Completed Chapters
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(NexoraCyan.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
                                        .border(1.dp, NexoraCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "$totalCompleted",
                                            color = NexoraCyan,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Completed",
                                            color = NexoraTextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Pending Chapters
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(NexoraPurple.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
                                        .border(1.dp, NexoraPurple.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "$totalPending",
                                            color = NexoraPurple,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Pending",
                                            color = NexoraTextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Total Chapter Points
                                Box(
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .background(NexoraGold.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
                                        .border(1.dp, NexoraGold.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "$totalEarnedPoints pts",
                                            color = NexoraGold,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Total Points",
                                            color = NexoraTextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Subject-Wise Breakdown Header
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Subject Breakdown & Chapters",
                        color = NexoraTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Real chapter progress, points, and pending lists across all 7 Commerce subjects",
                        color = NexoraTextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Render all 7 Subjects with completed and pending chapter names
            items(Subject.OFFICIAL_SUBJECTS, key = { it.id }) { subject ->
                val subjectChapters = remember(completedIds) { SyllabusRepository.getChapters(subject.id) }
                val completedCount = subjectChapters.count { completedIds.contains(it.id) }
                val totalCount = subjectChapters.size
                val subjectProgress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
                val subjectPercent = (subjectProgress * 100).toInt()
                val subjectEarnedPts = subjectChapters.filter { completedIds.contains(it.id) }.sumOf { it.points }
                val subjectMaxPts = subjectChapters.sumOf { it.points }
                val completedTitles = remember(completedIds) { SyllabusRepository.getCompletedChapterTitles(subject.id, completedIds) }
                val pendingTitles = remember(completedIds) { SyllabusRepository.getPendingChapterTitles(subject.id, completedIds) }
                val isExpanded = expandedSubjectMap[subject.id] ?: false

                NexoraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("friend_subject_card_${subject.id}"),
                    contentPadding = 14.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = subject.name,
                                        color = NexoraTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(NexoraSurfaceElevated, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = subject.code,
                                            color = NexoraCyan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$completedCount / $totalCount Chapters • $subjectEarnedPts / $subjectMaxPts pts",
                                    color = NexoraTextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            // Subject % Badge + Expand/Collapse Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (completedCount == totalCount && totalCount > 0) NexoraSuccess.copy(alpha = 0.15f)
                                            else NexoraCyan.copy(alpha = 0.12f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (completedCount == totalCount && totalCount > 0) NexoraSuccess.copy(alpha = 0.4f)
                                            else NexoraCyan.copy(alpha = 0.3f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "$subjectPercent%",
                                        color = if (completedCount == totalCount && totalCount > 0) NexoraSuccess else NexoraCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                IconButton(
                                    onClick = { expandedSubjectMap[subject.id] = !isExpanded },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        tint = NexoraTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { subjectProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (completedCount == totalCount && totalCount > 0) NexoraSuccess else NexoraCyan,
                            trackColor = NexoraSurfaceElevated,
                            strokeCap = StrokeCap.Round
                        )

                        // Quick Toggle Button to view chapter lists
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedSubjectMap[subject.id] = !isExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isExpanded) "Hide chapter details" else "View completed & pending chapters (${completedCount} done, ${totalCount - completedCount} pending)",
                                color = NexoraCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = NexoraCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Expandable Section: Completed & Pending Chapter Names
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Completed Chapters List
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = NexoraSuccess,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Completed Chapters (${completedTitles.size})",
                                            color = NexoraSuccess,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (completedTitles.isEmpty()) {
                                        Text(
                                            text = "No chapters completed in this subject yet.",
                                            color = NexoraTextMuted,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(start = 18.dp)
                                        )
                                    } else {
                                        completedTitles.forEachIndexed { idx, title ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 18.dp, top = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "✓ $title",
                                                    color = NexoraTextPrimary,
                                                    fontSize = 11.sp,
                                                    lineHeight = 15.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                // Pending Chapters List
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = NexoraPurple,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Pending Chapters (${pendingTitles.size})",
                                            color = NexoraPurple,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (pendingTitles.isEmpty()) {
                                        Text(
                                            text = "All chapters completed in this subject! 🎯",
                                            color = NexoraSuccess,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(start = 18.dp)
                                        )
                                    } else {
                                        pendingTitles.forEachIndexed { idx, title ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 18.dp, top = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "○ $title",
                                                    color = NexoraTextSecondary,
                                                    fontSize = 11.sp,
                                                    lineHeight = 15.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Privacy Assurance Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NexoraSurfaceElevated, RoundedCornerShape(12.dp))
                        .border(1.dp, NexoraBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Privacy Protected",
                        tint = NexoraCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Strict Privacy: Mobile numbers, Gmail, and account security details are confidential and never shared publicly.",
                        color = NexoraTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            // Bottom Message Button for Friends (if not self)
            if (onOpenChat != null && !profile.isCurrentStudent) {
                item {
                    Button(
                        onClick = onOpenChat,
                        colors = ButtonDefaults.buttonColors(containerColor = NexoraCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("friend_profile_bottom_chat_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = null,
                            tint = NexoraBackground,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Message ${profile.studentName}",
                            color = NexoraBackground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Backwards-compatible overload for Friend parameter
 */
@Composable
fun FriendProfileScreen(
    friend: Friend,
    onBackClick: () -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = remember(friend) {
        PublicStudentProfile(
            studentId = friend.id,
            studentName = friend.name,
            avatarInitials = friend.avatarInitials,
            avatarColorHex = friend.avatarColorHex,
            targetMarks = friend.targetMarks,
            completedTodoCount = friend.completedTodoCount,
            streakDays = friend.studyStreakDays,
            competitionPoints = friend.competitionPoints,
            leaderboardRank = friend.leaderboardRank,
            isOnline = friend.isOnline,
            isCurrentStudent = false,
            statusText = friend.statusText,
            completedChapterIds = friend.completedChapterIds
        )
    }

    FriendProfileScreen(
        profile = profile,
        onBackClick = onBackClick,
        onOpenChat = onOpenChat,
        modifier = modifier
    )
}

@Composable
private fun BigStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NexoraSurface)
            .border(1.dp, NexoraBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = NexoraTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = NexoraTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
