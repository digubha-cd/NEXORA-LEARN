package com.example.ui.screens.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.StudyProgressSummary
import com.example.core.model.SubjectProgressItem
import com.example.ui.components.NexoraCard
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraElectricBlue
import com.example.ui.theme.NexoraError
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraMagenta
import com.example.ui.theme.NexoraSuccess
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary

/**
 * Step 7 Progress & Study Streak Section:
 *
 * Requirements:
 * - Completed To-Do count
 * - Pending To-Do count
 * - Missed To-Do count
 * - Daily completion percentage
 * - Weekly completion percentage
 * - Subject-wise completed tasks
 * - Overall study progress
 * - Progress must be based only on the student's own To-Do activity.
 * - Study Streak: Current streak, Best streak, Today's completion status.
 * - Does not punish the student with automatic missed tasks.
 */
@Composable
fun StudyProgressSection(
    summary: StudyProgressSummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("study_progress_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. STUDY STREAK CARD
        StudyStreakCard(streak = summary.streak)

        // 2. SUMMARY COUNTERS (Completed, Pending, Missed, Total)
        TaskCountSummaryCard(
            completedCount = summary.completedCount,
            pendingCount = summary.pendingCount,
            missedCount = summary.missedCount,
            totalCount = summary.totalCount
        )

        // 3. COMPLETION PERCENTAGES (Daily, Weekly, Overall)
        CompletionPercentagesCard(
            dailyPercentage = summary.dailyPercentage,
            weeklyPercentage = summary.weeklyPercentage,
            overallPercentage = summary.overallPercentage
        )

        // 4. SUBJECT-WISE COMPLETED TASKS
        SubjectWiseProgressCard(
            subjectItems = summary.subjectProgressList
        )
    }
}

/**
 * Study Streak Card displaying current streak, best streak, and today's status.
 */
@Composable
fun StudyStreakCard(
    streak: com.example.core.model.StudyStreak,
    modifier: Modifier = Modifier
) {
    NexoraCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("study_streak_card"),
        containerColor = NexoraSurface,
        borderColor = if (streak.isCompletedToday) NexoraGold.copy(alpha = 0.5f) else NexoraBorder,
        contentPadding = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                if (streak.currentStreak > 0) NexoraGold.copy(alpha = 0.2f) else NexoraSurfaceElevated,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = if (streak.currentStreak > 0) NexoraGold else NexoraTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Study Streak",
                            color = NexoraTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Earned by completing tasks each day",
                            color = NexoraTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // Today's Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (streak.isCompletedToday) NexoraSuccess.copy(alpha = 0.15f)
                            else NexoraCyan.copy(alpha = 0.12f)
                        )
                        .border(
                            1.dp,
                            if (streak.isCompletedToday) NexoraSuccess.copy(alpha = 0.4f)
                            else NexoraCyan.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("today_streak_status_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (streak.isCompletedToday) Icons.Filled.CheckCircle else Icons.Outlined.HourglassBottom,
                            contentDescription = null,
                            tint = if (streak.isCompletedToday) NexoraSuccess else NexoraCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = streak.todayStatusText,
                            color = if (streak.isCompletedToday) NexoraSuccess else NexoraCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Streak Metric Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Current Streak
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(NexoraSurfaceElevated, RoundedCornerShape(12.dp))
                        .border(1.dp, NexoraBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Current Streak",
                            color = NexoraTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${streak.currentStreak}",
                                color = if (streak.currentStreak > 0) NexoraGold else NexoraTextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (streak.currentStreak == 1) "day" else "days",
                                color = NexoraTextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }
                }

                // Best Streak
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(NexoraSurfaceElevated, RoundedCornerShape(12.dp))
                        .border(1.dp, NexoraBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Best Streak",
                            color = NexoraTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${streak.bestStreak}",
                                color = NexoraCyan,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (streak.bestStreak == 1) "day" else "days",
                                color = NexoraTextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }
                }
            }

            // Explanatory Note
            Text(
                text = if (streak.isCompletedToday)
                    "🔥 Great job! You completed a study task today. Keep it up tomorrow!"
                else
                    "Complete at least 1 student-created To-Do today to maintain or increase your streak.",
                color = NexoraTextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Card showing Completed, Pending, Missed, and Total To-Do counts.
 */
@Composable
private fun TaskCountSummaryCard(
    completedCount: Int,
    pendingCount: Int,
    missedCount: Int,
    totalCount: Int
) {
    NexoraCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_counts_summary_card"),
        containerColor = NexoraSurface,
        contentPadding = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Study To-Do Overview",
                color = NexoraTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Completed
                CountMetricBox(
                    modifier = Modifier.weight(1f),
                    label = "Completed",
                    count = completedCount,
                    accentColor = NexoraSuccess,
                    testTag = "metric_completed_count"
                )

                // Pending
                CountMetricBox(
                    modifier = Modifier.weight(1f),
                    label = "Pending",
                    count = pendingCount,
                    accentColor = NexoraCyan,
                    testTag = "metric_pending_count"
                )

                // Missed
                CountMetricBox(
                    modifier = Modifier.weight(1f),
                    label = "Missed",
                    count = missedCount,
                    accentColor = if (missedCount > 0) NexoraError else NexoraTextMuted,
                    testTag = "metric_missed_count"
                )

                // Total
                CountMetricBox(
                    modifier = Modifier.weight(1f),
                    label = "Total",
                    count = totalCount,
                    accentColor = NexoraElectricBlue,
                    testTag = "metric_total_count"
                )
            }
        }
    }
}

@Composable
private fun CountMetricBox(
    label: String,
    count: Int,
    accentColor: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(NexoraSurfaceElevated, RoundedCornerShape(10.dp))
            .border(1.dp, NexoraBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$count",
                color = accentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = NexoraTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Card showing Daily, Weekly, and Overall completion percentages.
 */
@Composable
private fun CompletionPercentagesCard(
    dailyPercentage: Int,
    weeklyPercentage: Int,
    overallPercentage: Int
) {
    NexoraCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("completion_percentages_card"),
        containerColor = NexoraSurface,
        contentPadding = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Study Completion Rates",
                    color = NexoraTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Based on your To-Do tasks",
                    color = NexoraTextMuted,
                    fontSize = 11.sp
                )
            }

            // Daily Progress
            ProgressRow(
                label = "Daily Completion",
                subtext = "Tasks scheduled for today",
                percentage = dailyPercentage,
                barColor = NexoraCyan
            )

            // Weekly Progress
            ProgressRow(
                label = "Weekly Completion",
                subtext = "Current calendar week (Mon–Sun)",
                percentage = weeklyPercentage,
                barColor = NexoraGold
            )

            // Overall Progress
            ProgressRow(
                label = "Overall Study Progress",
                subtext = "All student-created study tasks",
                percentage = overallPercentage,
                barColor = NexoraSuccess
            )
        }
    }
}

@Composable
private fun ProgressRow(
    label: String,
    subtext: String,
    percentage: Int,
    barColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    color = NexoraTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtext,
                    color = NexoraTextMuted,
                    fontSize = 10.sp
                )
            }
            Text(
                text = "$percentage%",
                color = barColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = { (percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = NexoraSurfaceElevated
        )
    }
}

/**
 * Card showing Subject-wise completed tasks for all 7 official Class 12 Commerce subjects.
 */
@Composable
private fun SubjectWiseProgressCard(
    subjectItems: List<SubjectProgressItem>
) {
    NexoraCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_wise_progress_card"),
        containerColor = NexoraSurface,
        contentPadding = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        tint = NexoraCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Subject-Wise Progress",
                        color = NexoraTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "7 Subjects",
                    color = NexoraTextMuted,
                    fontSize = 11.sp
                )
            }

            Text(
                text = "Progress is calculated strictly from your own study To-Do items assigned to each subject.",
                color = NexoraTextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                subjectItems.forEach { item ->
                    SubjectProgressRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun SubjectProgressRow(item: SubjectProgressItem) {
    val subjectColor = when (item.subjectId) {
        "accounts" -> NexoraCyan
        "stat" -> NexoraElectricBlue
        "economics" -> NexoraGold
        "ba" -> NexoraSuccess
        "sp_cc" -> NexoraMagenta
        "english" -> Color(0xFF38BDF8)
        "gujarati" -> Color(0xFFA855F7)
        else -> NexoraCyan
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NexoraSurfaceElevated, RoundedCornerShape(10.dp))
            .border(1.dp, NexoraBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
            .testTag("subject_progress_row_${item.subjectId}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(subjectColor, CircleShape)
                    )
                    Text(
                        text = item.subjectName,
                        color = NexoraTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "${item.completedCount} / ${item.totalCount} completed (${item.progressPercentage}%)",
                    color = if (item.completedCount > 0) subjectColor else NexoraTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            LinearProgressIndicator(
                progress = { (item.progressPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = subjectColor,
                trackColor = NexoraBackground
            )
        }
    }
}
