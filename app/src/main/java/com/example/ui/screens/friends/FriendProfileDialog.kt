package com.example.ui.screens.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Friend
import com.example.core.model.PublicStudentProfile
import com.example.core.model.Subject
import com.example.core.repository.SyllabusRepository
import com.example.ui.components.NexoraTargetBadge
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraPink
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary

/**
 * Friend Profile Dialog (Step 8A).
 *
 * Privacy Guarantees:
 * Friends can see:
 * - Name
 * - Profile photo / avatar
 * - 90+ target
 * - Overall & subject-wise study progress
 * - Completed To-Do count
 * - Study streak
 * - Competition points
 * - Leaderboard rank
 *
 * Keep Private (Strictly Omitted):
 * - Mobile number
 * - Gmail / Email
 * - Login / authentication credentials
 * - Account settings
 */
@Composable
fun FriendProfileDialog(
    friend: Friend,
    onDismiss: () -> Unit,
    onOpenChat: () -> Unit
) {
    val completedIds = friend.completedChapterIds
    val totalChapters = remember(completedIds) { SyllabusRepository.getTotalChaptersCount() }
    val totalCompleted = remember(completedIds) { SyllabusRepository.getTotalCompletedChaptersCount(completedIds) }
    val overallProgress = remember(completedIds) { SyllabusRepository.getOverallProgress(completedIds) }
    val overallPercentInt = (overallProgress * 100).toInt()
    val totalEarnedPoints = remember(completedIds) { SyllabusRepository.getTotalEarnedPoints(completedIds) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("friend_profile_dialog"),
        containerColor = NexoraSurfaceVariant,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Friend Profile",
                    color = NexoraTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = NexoraTextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar + Status
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(friend.avatarColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = friend.avatarInitials,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = friend.name,
                    color = NexoraTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${friend.statusText} • Gujarati Medium",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Academic Target Badge & Rank Badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NexoraTargetBadge(
                        targetTitle = friend.targetMarks,
                        compact = true
                    )

                    if (friend.leaderboardRank > 0) {
                        Box(
                            modifier = Modifier
                                .background(NexoraGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, NexoraGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = null,
                                    tint = NexoraGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Rank #${friend.leaderboardRank}",
                                    color = NexoraGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Objective Stats Grid: To-Dos, Streak, Points
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "To-Dos",
                        value = "${friend.completedTodoCount}",
                        icon = Icons.Outlined.CheckCircle,
                        tint = NexoraCyan,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Streak",
                        value = "${friend.studyStreakDays}d",
                        icon = Icons.Filled.Whatshot,
                        tint = NexoraGold,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Points",
                        value = "${friend.competitionPoints}",
                        icon = Icons.Filled.EmojiEvents,
                        tint = NexoraPink,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Overall Syllabus Coverage Summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NexoraSurfaceElevated, RoundedCornerShape(10.dp))
                        .border(1.dp, NexoraBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overall Study Progress",
                            color = NexoraTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$overallPercentInt% ($totalCompleted/$totalChapters)",
                            color = NexoraCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NexoraCyan,
                        trackColor = NexoraSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total Chapter Points: $totalEarnedPoints pts",
                        color = NexoraGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subject-Wise Study Progress Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All 7 Subjects Progress",
                        color = NexoraTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Class 12 Commerce",
                        color = NexoraTextMuted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subject Progress Bars for 7 Commerce subjects
                Subject.OFFICIAL_SUBJECTS.forEach { subject ->
                    val subjectChapters = SyllabusRepository.getChapters(subject.id)
                    val completedCount = subjectChapters.count { completedIds.contains(it.id) }
                    val totalCount = subjectChapters.size
                    val progressFloat = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
                    val progressPercent = (progressFloat * 100).toInt()
                    val earnedPts = subjectChapters.filter { completedIds.contains(it.id) }.sumOf { it.points }
                    val maxPts = subjectChapters.sumOf { it.points }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = subject.name,
                                color = NexoraTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$completedCount/$totalCount ($progressPercent% • $earnedPts pts)",
                                color = NexoraCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { progressFloat },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = NexoraCyan,
                            trackColor = NexoraSurfaceElevated
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Privacy Assurance Note
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NexoraSurfaceElevated, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Privacy Protected",
                        tint = NexoraCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mobile number and account info are strictly private.",
                        color = NexoraTextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onOpenChat()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexoraCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("friend_profile_chat_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = null,
                    tint = NexoraBackground,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Message",
                    color = NexoraBackground,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    )
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(NexoraSurfaceElevated, RoundedCornerShape(10.dp))
            .border(1.dp, NexoraBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = NexoraTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            color = NexoraTextMuted,
            fontSize = 10.sp
        )
    }
}
