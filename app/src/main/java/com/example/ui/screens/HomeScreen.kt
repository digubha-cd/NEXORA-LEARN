package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.constants.AppConstants
import com.example.core.model.ExamType
import com.example.core.model.Friend
import com.example.core.model.StudentProfile
import com.example.core.model.Subject
import com.example.core.repository.FriendsRepository
import com.example.core.repository.StudyPlannerRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NexoraCard
import com.example.ui.components.NexoraLogo
import com.example.ui.components.NexoraTargetBadge
import com.example.ui.screens.friends.HomeFriendsSection
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraElectricBlue
import com.example.ui.theme.NexoraError
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraMagenta
import com.example.ui.theme.NexoraPink
import com.example.ui.theme.NexoraSuccess
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary

/**
 * Foundational Home Dashboard for NEXORA LEARN.
 * Displays real authenticated student profile, official exam dates,
 * empty state for unpopulated tasks, and the official 7 Class 12 Commerce subjects.
 */
@Composable
fun HomeScreen(
    studentProfile: StudentProfile,
    onSignOut: () -> Unit,
    onSubjectClick: (Subject) -> Unit = {},
    onNavigateToPlanner: () -> Unit = {},
    onNavigateToExam: () -> Unit = {},
    onOpenFriendsPage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tasks by StudyPlannerRepository.tasks.collectAsStateWithLifecycle()
    val streak = remember(tasks) { StudyPlannerRepository.getStudyStreak() }
    val todayTasks = remember(tasks) { StudyPlannerRepository.getTodayTasks() }
    val missedTasks = remember(tasks) { StudyPlannerRepository.getMissedTasks() }
    val todayCompletedCount = remember(todayTasks) { todayTasks.count { it.isCompleted } }
    val todayTotalCount = todayTasks.size
    val dailyProgress = if (todayTotalCount > 0) todayCompletedCount.toFloat() / todayTotalCount else 0.0f

    // Friends & Social State
    val friendsList by FriendsRepository.friends.collectAsStateWithLifecycle()
    val totalNotificationCount = remember(friendsList) { FriendsRepository.getTotalNotificationCount() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NexoraBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("home_screen_root")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Top Header: Branding + Student Info + Sign Out
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_top_bar"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NexoraLogo(
                            emblemSize = 42.dp,
                            showWordmark = false,
                            showSubtitle = false
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = AppConstants.APP_NAME,
                                color = NexoraTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Welcome, ${studentProfile.studentName}",
                                color = NexoraCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = NexoraTextMuted
                        )
                    }
                }
            }

            // Highlighted Main Academic Target: 90+ MARKS
            item {
                NexoraTargetBadge(
                    modifier = Modifier.fillMaxWidth(),
                    targetText = studentProfile.targetMarks,
                    subtitle = "GSEB ${studentProfile.classLevel} • ${studentProfile.medium}"
                )
            }

            // Student Status Card
            item {
                NexoraCard(
                    modifier = Modifier.testTag("student_status_card"),
                    contentPadding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = NexoraCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = studentProfile.studentName,
                                    color = NexoraTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enrolled: ${studentProfile.medium}",
                                color = NexoraTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        // Verified Status
                        Box(
                            modifier = Modifier
                                .background(NexoraCyan.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, NexoraCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = NexoraCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Active",
                                    color = NexoraCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Configurable Exam Milestones Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exam Milestones",
                        color = NexoraTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("exam_milestones_title")
                    )
                    TextButton(
                        onClick = onNavigateToExam,
                        modifier = Modifier.testTag("view_exam_dashboard_btn")
                    ) {
                        Text(
                            text = "Exam Dashboard",
                            color = NexoraCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = NexoraCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // School Exam Card
                    NexoraCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToExam() }
                            .testTag("school_exam_card"),
                        contentPadding = 14.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(NexoraCyan.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = null,
                                    tint = NexoraCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppConstants.SCHOOL_EXAM_NAME,
                                color = NexoraTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = AppConstants.SCHOOL_EXAM_DATE,
                            color = NexoraCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = StudyPlannerRepository.formatExamCountdown(
                                StudyPlannerRepository.getSchoolExamDaysRemaining(),
                                AppConstants.SCHOOL_EXAM_NAME
                            ),
                            color = NexoraCyan.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Board Exam Card
                    NexoraCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToExam() }
                            .testTag("board_exam_card"),
                        contentPadding = 14.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(NexoraMagenta.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = null,
                                    tint = NexoraMagenta,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppConstants.BOARD_EXAM_NAME,
                                color = NexoraTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = AppConstants.BOARD_EXAM_DATE,
                            color = NexoraMagenta,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = StudyPlannerRepository.formatExamCountdown(
                                StudyPlannerRepository.getBoardExamDaysRemaining(),
                                AppConstants.BOARD_EXAM_NAME
                            ),
                            color = NexoraMagenta.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Study Streak Card
            item {
                NexoraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_study_streak_card"),
                    containerColor = if (streak.currentStreak > 0) NexoraGold.copy(alpha = 0.08f) else NexoraSurface,
                    borderColor = if (streak.currentStreak > 0) NexoraGold.copy(alpha = 0.35f) else NexoraBorder,
                    contentPadding = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (streak.currentStreak > 0) NexoraGold.copy(alpha = 0.2f) else NexoraSurfaceElevated,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (streak.currentStreak > 0) Icons.Filled.Whatshot else Icons.Outlined.Whatshot,
                                    contentDescription = null,
                                    tint = if (streak.currentStreak > 0) NexoraGold else NexoraTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${streak.currentStreak} Day${if (streak.currentStreak == 1) "" else "s"} Streak",
                                        color = if (streak.currentStreak > 0) NexoraGold else NexoraTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (streak.currentStreak > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "🔥", fontSize = 14.sp)
                                    }
                                }
                                Text(
                                    text = if (streak.isCompletedToday) "Completed a study task today! 🎉" else "Complete 1 task today to keep streak",
                                    color = NexoraTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Best Streak & Status Badge
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Best: ${streak.bestStreak}d",
                                color = NexoraTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (streak.isCompletedToday) NexoraSuccess.copy(alpha = 0.15f) else NexoraSurfaceElevated,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (streak.isCompletedToday) NexoraSuccess.copy(alpha = 0.35f) else NexoraBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (streak.isCompletedToday) "Today Done ✓" else "Today Pending",
                                    color = if (streak.isCompletedToday) NexoraSuccess else NexoraTextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Study Friends Card (Step 8A)
            item {
                HomeFriendsSection(
                    friends = friendsList,
                    totalNotificationCount = totalNotificationCount,
                    onOpenFriendsPage = onOpenFriendsPage
                )
            }

            // Today's Tasks Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Study To-Do",
                        color = NexoraTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("study_plan_section_title")
                    )

                    TextButton(
                        onClick = onNavigateToPlanner,
                        modifier = Modifier.testTag("open_planner_button")
                    ) {
                        Text(
                            text = "Open Planner",
                            color = NexoraCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = NexoraCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Daily Study Progress Card on Home Screen
            item {
                NexoraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_daily_progress_card"),
                    contentPadding = 14.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today • ${StudyPlannerRepository.TODAY_FORMATTED}",
                                color = NexoraTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$todayCompletedCount / $todayTotalCount Tasks Done",
                                color = if (todayCompletedCount == todayTotalCount && todayTotalCount > 0) NexoraSuccess else NexoraCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (todayTotalCount > 0) {
                            LinearProgressIndicator(
                                progress = { dailyProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (dailyProgress == 1.0f) NexoraSuccess else NexoraCyan,
                                trackColor = NexoraSurfaceVariant
                            )
                        }

                        Text(
                            text = if (todayTotalCount > 0)
                                "${(dailyProgress * 100).toInt()}% completed of today's study tasks"
                            else
                                "Your study plan will appear here. Tap Open Planner to add tasks.",
                            color = NexoraTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Missed Tasks Alert on Home (if any)
            if (missedTasks.isNotEmpty()) {
                item {
                    NexoraCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = NexoraError.copy(alpha = 0.12f),
                        borderColor = NexoraError.copy(alpha = 0.4f),
                        contentPadding = 12.dp,
                        onClick = onNavigateToPlanner
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Warning,
                                    contentDescription = null,
                                    tint = NexoraError,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${missedTasks.size} Missed Task(s)",
                                        color = NexoraError,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tap to open planner and reschedule",
                                        color = NexoraTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Text(
                                text = "Reschedule",
                                color = NexoraCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Today's Tasks Empty State if no tasks for today
            if (todayTasks.isEmpty()) {
                item {
                    NexoraCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPlanner() }
                            .testTag("home_tasks_empty_card"),
                        contentPadding = 18.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NexoraCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AssignmentTurnedIn,
                                    contentDescription = null,
                                    tint = NexoraCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "No study tasks for today",
                                    color = NexoraTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Tap to create your personalized study To-Do in Planner",
                                    color = NexoraTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Today's Tasks Cards
            items(todayTasks, key = { it.taskId }) { task ->
                NexoraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_task_${task.taskId}"),
                    containerColor = if (task.isCompleted) NexoraSurfaceElevated.copy(alpha = 0.5f) else NexoraSurface,
                    borderColor = if (task.isCompleted) NexoraCyan.copy(alpha = 0.3f) else NexoraBorder,
                    contentPadding = 12.dp,
                    onClick = { StudyPlannerRepository.toggleTaskCompletion(task.taskId) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { StudyPlannerRepository.toggleTaskCompletion(task.taskId) },
                            modifier = Modifier.testTag("home_task_checkbox_${task.taskId}"),
                            colors = CheckboxDefaults.colors(
                                checkedColor = NexoraCyan,
                                uncheckedColor = NexoraBorder,
                                checkmarkColor = NexoraBackground
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (task.subjectName.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .background(NexoraCyan.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = task.subjectName,
                                            color = NexoraCyan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = if (task.examPhase == ExamType.SCHOOL_EXAM) "School Exam (22 Oct)" else "Board Exam",
                                    color = NexoraTextMuted,
                                    fontSize = 10.sp
                                )

                                if (task.hasReminder) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier
                                            .background(NexoraCyan.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                            .border(1.dp, NexoraCyan.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Notifications,
                                            contentDescription = "Reminder",
                                            tint = NexoraCyan,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = task.reminderFormatted.ifBlank { "Reminder" },
                                            color = NexoraCyan,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = task.title.ifEmpty { task.chapterTitle },
                                color = if (task.isCompleted) NexoraTextMuted else NexoraTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Official Subjects Foundation (7 Class 12 Commerce subjects)
            item {
                Text(
                    text = "Class 12 Commerce Subjects",
                    color = NexoraTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("subjects_section_title")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Official Gujarat Board textbook syllabus & chapters loaded.",
                    color = NexoraTextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(Subject.OFFICIAL_SUBJECTS, key = { it.id }) { subject ->
                NexoraCard(
                    onClick = { onSubjectClick(subject) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_card_${subject.id}"),
                    contentPadding = 12.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NexoraSurfaceElevated, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MenuBook,
                                    contentDescription = null,
                                    tint = NexoraCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = subject.name,
                                    color = NexoraTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Code: ${subject.code} • Gujarati Medium",
                                    color = NexoraTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Readiness indicator
                        Box(
                            modifier = Modifier
                                .background(NexoraSurfaceElevated, shape = RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Syllabus Ready",
                                color = NexoraCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
