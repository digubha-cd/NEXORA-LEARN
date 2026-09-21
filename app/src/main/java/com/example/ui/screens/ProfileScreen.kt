package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.constants.AppConstants
import com.example.core.model.StudentProfile
import com.example.core.model.Subject
import com.example.core.repository.SyllabusRepository
import com.example.ui.components.NexoraCard
import com.example.ui.components.NexoraLogo
import com.example.ui.components.NexoraSecondaryButton
import com.example.ui.components.NexoraTargetBadge
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraError
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary

/**
 * Profile screen displaying student enrollment info, Gujarati Medium status,
 * 90+ Marks academic target, full live Study Progress from SyllabusRepository, and session management.
 */
@Composable
fun ProfileScreen(
    studentProfile: StudentProfile,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Live observation of actual chapter checkboxes from SyllabusRepository
    val completedChapterIds by SyllabusRepository.completedChapterIds.collectAsStateWithLifecycle()

    val totalChapters = remember(completedChapterIds) { SyllabusRepository.getTotalChaptersCount() }
    val totalCompleted = remember(completedChapterIds) { SyllabusRepository.getTotalCompletedChaptersCount() }
    val totalPending = remember(completedChapterIds) { SyllabusRepository.getTotalPendingChaptersCount() }
    val overallProgress = remember(completedChapterIds) { SyllabusRepository.getOverallProgress() }
    val overallPercentInt = (overallProgress * 100).toInt()
    val totalEarnedPoints = remember(completedChapterIds) { SyllabusRepository.getTotalEarnedPoints() }
    val maxPossiblePoints = remember(completedChapterIds) { SyllabusRepository.getMaxPossiblePoints() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NexoraBackground)
            .statusBarsPadding()
            .testTag("profile_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Student Profile",
                            color = NexoraTextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = AppConstants.BOARD_NAME,
                            color = NexoraTextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    NexoraLogo(
                        emblemSize = 36.dp,
                        showWordmark = false,
                        showSubtitle = false
                    )
                }
            }

            // Student Identity Card
            item {
                NexoraCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 18.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(NexoraCyan.copy(alpha = 0.15f), shape = CircleShape)
                                .border(1.5.dp, NexoraCyan.copy(alpha = 0.5f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = NexoraCyan,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = studentProfile.studentName,
                                color = NexoraTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = NexoraCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = studentProfile.medium,
                                    color = NexoraCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Prominent 90+ Marks Target Badge
            item {
                NexoraTargetBadge(
                    modifier = Modifier.fillMaxWidth(),
                    targetText = studentProfile.targetMarks,
                    subtitle = "GSEB Class 12 Commerce Board Target"
                )
            }

            // --- FULL STUDY PROGRESS SECTION (LIVE FROM SYLLABUS REPOSITORY) ---
            item {
                Column(modifier = Modifier.fillMaxWidth().testTag("profile_study_progress_section")) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
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

                    // Overall Progress Summary Card
                    NexoraCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 16.dp
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Progress bar
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
                                        .clip(RoundedCornerShape(4.dp))
                                        .testTag("overall_progress_bar"),
                                    color = NexoraCyan,
                                    trackColor = NexoraSurfaceElevated,
                                    strokeCap = StrokeCap.Round
                                )
                            }

                            // 3 KPI Highlights: Completed Chapters, Pending Chapters, Total Chapter Points
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
                                        .testTag("kpi_completed_chapters")
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
                                        .testTag("kpi_pending_chapters")
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
                                        .testTag("kpi_chapter_points")
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

            // Subject-Wise Progress Breakdown for All 7 Subjects
            item {
                Text(
                    text = "Subject Breakdown",
                    color = NexoraTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Live chapter completion and earned points across 7 Commerce subjects",
                    color = NexoraTextMuted,
                    fontSize = 12.sp
                )
            }

            // Render all 7 Subjects
            items(Subject.OFFICIAL_SUBJECTS, key = { it.id }) { subject ->
                val subjectChapters = remember { SyllabusRepository.getChapters(subject.id) }
                val completedCount = subjectChapters.count { completedChapterIds.contains(it.id) }
                val totalCount = subjectChapters.size
                val subjectProgress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
                val subjectPercent = (subjectProgress * 100).toInt()
                val subjectEarnedPts = subjectChapters.filter { completedChapterIds.contains(it.id) }.sumOf { it.points }
                val subjectMaxPts = subjectChapters.sumOf { it.points }

                NexoraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_progress_card_${subject.id}"),
                    contentPadding = 14.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(NexoraSurfaceElevated, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Code ${subject.code}",
                                            color = NexoraTextMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Subject Points Tag
                            Box(
                                modifier = Modifier
                                    .background(NexoraGold.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .border(0.8.dp, NexoraGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$subjectEarnedPts / $subjectMaxPts pts",
                                    color = NexoraGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Progress Bar & Stats
                        LinearProgressIndicator(
                            progress = { subjectProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (subjectProgress >= 1f) NexoraCyan else NexoraCyan.copy(alpha = 0.85f),
                            trackColor = NexoraSurfaceElevated,
                            strokeCap = StrokeCap.Round
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$completedCount of $totalCount chapters completed",
                                color = NexoraTextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "$subjectPercent%",
                                color = if (subjectProgress >= 1f) NexoraCyan else NexoraTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Academic Details
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Academic Details",
                    color = NexoraTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                NexoraCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 16.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Class & Stream
                        ProfileDetailRow(
                            icon = Icons.Outlined.School,
                            label = "Class & Stream",
                            value = studentProfile.classLevel
                        )

                        // Medium
                        ProfileDetailRow(
                            icon = Icons.Outlined.Language,
                            label = "Medium of Study",
                            value = studentProfile.medium
                        )

                        // Contact Email (if present)
                        if (!studentProfile.email.isNullOrEmpty()) {
                            ProfileDetailRow(
                                icon = Icons.Outlined.Email,
                                label = "Email",
                                value = studentProfile.email
                            )
                        }

                        // Contact Phone (if present)
                        if (!studentProfile.phoneNumber.isNullOrEmpty()) {
                            ProfileDetailRow(
                                icon = Icons.Outlined.Phone,
                                label = "Phone Number",
                                value = studentProfile.phoneNumber
                            )
                        }

                        // Target Marks
                        ProfileDetailRow(
                            icon = Icons.Outlined.Stars,
                            label = "Target Marks",
                            value = studentProfile.targetMarks,
                            valueColor = NexoraGold
                        )
                    }
                }
            }

            // Sign Out Option
            item {
                Spacer(modifier = Modifier.height(8.dp))
                NexoraSecondaryButton(
                    text = "Sign Out",
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_sign_out_button")
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = NexoraTextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(NexoraSurfaceElevated, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NexoraCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                color = NexoraTextSecondary,
                fontSize = 13.sp
            )
        }

        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
