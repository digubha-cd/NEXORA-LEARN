package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.ExamType
import com.example.core.model.PlannerFilterTab
import com.example.core.model.StudyTask
import com.example.core.model.TaskPriority
import com.example.core.model.TaskStatus
import com.example.core.repository.StudyPlannerRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NexoraCard
import com.example.ui.components.NexoraLogo
import com.example.ui.components.NexoraTargetBadge
import com.example.ui.screens.planner.AddEditTaskDialog
import com.example.ui.screens.planner.DailyStudyReminderCard
import com.example.ui.screens.planner.PlannerCalendarView
import com.example.ui.screens.planner.StudyProgressSection
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraElectricBlue
import com.example.ui.theme.NexoraError
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraMagenta
import com.example.ui.theme.NexoraPink
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraSuccess
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary
import java.time.LocalDate

private fun getSubjectColor(subjectId: String): Color {
    return when (subjectId) {
        "gujarati" -> NexoraCyan
        "english" -> NexoraElectricBlue
        "sp_cc" -> NexoraPurple
        "ba" -> NexoraMagenta
        "stat" -> NexoraCyan
        "accounts" -> NexoraGold
        "economics" -> NexoraPink
        else -> NexoraCyan
    }
}

/**
 * Study Planner Screen for Gujarat Board Class 12 Commerce.
 *
 * Student-Created To-Do & Automatic Exam Countdowns:
 * - NO automatic study task generation.
 * - Students create, edit, delete, mark complete, and optionally assign dates to their tasks.
 * - Calendar shows only student-created tasks, completed tasks, selected date, and exam dates.
 * - Exam Countdowns are completely automatic:
 *     School Exam — 22 October 2026
 *     Board Exam — 25 February 2027
 *   Decreases by exactly 1 day each calendar day, shows "EXAM DAY" on the day, never negative.
 */
@Composable
fun StudyPlannerScreen(
    modifier: Modifier = Modifier,
    onNavigateToSubjects: () -> Unit = {}
) {
    val context = LocalContext.current
    val tasks by StudyPlannerRepository.tasks.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(PlannerFilterTab.PENDING) }
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<StudyTask?>(null) }
    var dialogInitialDate by remember { mutableStateOf<LocalDate?>(null) }

    // Metrics
    val pendingTasks = remember(tasks) { tasks.filter { !it.isCompleted } }
    val completedTasks = remember(tasks) { tasks.filter { it.isCompleted } }
    val totalCount = tasks.size
    val completedCount = completedTasks.size
    val progressRatio = if (totalCount > 0) completedCount.toFloat() / totalCount else 0.0f

    // Countdowns
    val today = StudyPlannerRepository.TODAY
    val schoolDays = StudyPlannerRepository.getSchoolExamDaysRemaining(today)
    val boardDays = StudyPlannerRepository.getBoardExamDaysRemaining(today)
    val schoolCountdownText = StudyPlannerRepository.formatDays(schoolDays)
    val boardCountdownText = StudyPlannerRepository.formatDays(boardDays)

    // Active displayed tasks based on tab
    val displayedTasks: List<StudyTask> = remember(selectedTab, tasks) {
        when (selectedTab) {
            PlannerFilterTab.PENDING -> pendingTasks
            PlannerFilterTab.COMPLETED -> completedTasks
            PlannerFilterTab.ALL -> tasks
            PlannerFilterTab.TODAY -> tasks.filter { it.scheduledDateStr == StudyPlannerRepository.TODAY_STR }
            PlannerFilterTab.UPCOMING -> StudyPlannerRepository.getUpcomingTasks()
            PlannerFilterTab.MISSED -> StudyPlannerRepository.getMissedTasks()
            PlannerFilterTab.CALENDAR, PlannerFilterTab.SUBJECTS_TODO, PlannerFilterTab.PROGRESS -> emptyList()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NexoraBackground)
            .statusBarsPadding()
            .testTag("study_planner_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title + Subtitle + Logo
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Study Planner",
                            color = NexoraTextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Class 12 Commerce • Student To-Do System",
                            color = NexoraTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    NexoraLogo(
                        emblemSize = 38.dp,
                        showWordmark = false,
                        showSubtitle = false
                    )
                }
            }

            // Prominent 90+ MARKS Target Highlight
            item {
                NexoraTargetBadge(
                    modifier = Modifier.fillMaxWidth(),
                    targetText = "90+ MARKS TARGET",
                    subtitle = "GSEB Class 12 Commerce Academic Standard"
                )
            }

            // Automatic Exam Countdown Cards
            item {
                NexoraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("automatic_exam_countdown_card"),
                    contentPadding = 14.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Exam Countdown",
                                color = NexoraTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Automatic Daily Countdown",
                                color = NexoraCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // School Exam (22 Oct 2026)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NexoraCyan.copy(alpha = 0.1f))
                                    .border(1.dp, NexoraCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                                    .testTag("school_exam_countdown_badge")
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.School,
                                            contentDescription = null,
                                            tint = NexoraCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "School Exam",
                                            color = NexoraCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "22 October 2026",
                                        color = NexoraTextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = if (schoolCountdownText == "EXAM DAY") "EXAM DAY" else "$schoolCountdownText remaining",
                                        color = if (schoolCountdownText == "EXAM DAY") NexoraGold else NexoraTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.testTag("school_exam_days_text")
                                    )
                                }
                            }

                            // Board Exam (25 Feb 2027)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NexoraGold.copy(alpha = 0.1f))
                                    .border(1.dp, NexoraGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                                    .testTag("board_exam_countdown_badge")
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Star,
                                            contentDescription = null,
                                            tint = NexoraGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Board Exam",
                                            color = NexoraGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "25 February 2027",
                                        color = NexoraTextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = if (boardCountdownText == "EXAM DAY") "EXAM DAY" else "$boardCountdownText remaining",
                                        color = NexoraGold,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.testTag("board_exam_days_text")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Daily Planned Study Sessions Reminder Scheduling Card
            item {
                val todayTasksList = remember(tasks) {
                    tasks.filter { it.scheduledDateStr == StudyPlannerRepository.TODAY_STR }
                }
                DailyStudyReminderCard(
                    todayTasks = todayTasksList,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Student To-Do Action Bar (+ Add To-Do)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "My Study To-Do",
                            color = NexoraTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (totalCount == 0) "Create your personalized study tasks" else "$completedCount of $totalCount tasks completed (${(progressRatio * 100).toInt()}%)",
                            color = NexoraTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            taskToEdit = null
                            dialogInitialDate = null
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NexoraCyan,
                            contentColor = NexoraBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_todo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add To-Do",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Progress Bar if student has tasks
            if (totalCount > 0) {
                item {
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NexoraCyan,
                        trackColor = NexoraSurfaceVariant
                    )
                }
            }

            // Primary Navigation Tabs (Pending, Completed, Calendar, All Tasks)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlannerTabButton(
                        tab = PlannerFilterTab.PENDING,
                        label = "Pending",
                        count = pendingTasks.size,
                        isSelected = selectedTab == PlannerFilterTab.PENDING,
                        onClick = { selectedTab = PlannerFilterTab.PENDING }
                    )

                    PlannerTabButton(
                        tab = PlannerFilterTab.COMPLETED,
                        label = "Completed",
                        count = completedTasks.size,
                        isSelected = selectedTab == PlannerFilterTab.COMPLETED,
                        onClick = { selectedTab = PlannerFilterTab.COMPLETED }
                    )

                    PlannerTabButton(
                        tab = PlannerFilterTab.CALENDAR,
                        label = "Calendar",
                        count = totalCount,
                        isSelected = selectedTab == PlannerFilterTab.CALENDAR,
                        onClick = { selectedTab = PlannerFilterTab.CALENDAR }
                    )

                    PlannerTabButton(
                        tab = PlannerFilterTab.ALL,
                        label = "All Tasks",
                        count = totalCount,
                        isSelected = selectedTab == PlannerFilterTab.ALL,
                        onClick = { selectedTab = PlannerFilterTab.ALL }
                    )

                    PlannerTabButton(
                        tab = PlannerFilterTab.PROGRESS,
                        label = "Progress",
                        count = null,
                        isSelected = selectedTab == PlannerFilterTab.PROGRESS,
                        onClick = { selectedTab = PlannerFilterTab.PROGRESS }
                    )
                }
            }

            // --- TAB CONTENT 1: PROGRESS SECTION ---
            if (selectedTab == PlannerFilterTab.PROGRESS) {
                item {
                    val progressSummary = remember(tasks) {
                        StudyPlannerRepository.getProgressSummary()
                    }
                    StudyProgressSection(summary = progressSummary)
                }
            }

            // --- TAB CONTENT 2: CALENDAR VIEW ---
            else if (selectedTab == PlannerFilterTab.CALENDAR) {
                item {
                    PlannerCalendarView(
                        tasks = tasks,
                        onToggleTaskCompletion = { taskId ->
                            StudyPlannerRepository.toggleTaskCompletion(taskId)
                        },
                        onSelectTask = { task ->
                            taskToEdit = task
                            showAddDialog = true
                        },
                        onAddTaskForDate = { date ->
                            taskToEdit = null
                            dialogInitialDate = date
                            showAddDialog = true
                        }
                    )
                }
            }

            // --- TAB CONTENT 3: TASK LISTS (Pending, Completed, All Tasks) ---
            else {
                // Section Subheader
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (selectedTab) {
                                PlannerFilterTab.PENDING -> "Pending Study Tasks"
                                PlannerFilterTab.COMPLETED -> "Completed Study Tasks"
                                PlannerFilterTab.ALL -> "All Study Tasks"
                                else -> "Tasks"
                            },
                            color = NexoraTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${displayedTasks.size} task(s)",
                            color = NexoraTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                // Task List / Empty State
                if (displayedTasks.isEmpty()) {
                    item {
                        val emptyTitle = when (selectedTab) {
                            PlannerFilterTab.PENDING -> if (totalCount == 0) "No study tasks yet" else "All tasks completed!"
                            PlannerFilterTab.COMPLETED -> "No completed tasks yet"
                            else -> "No study tasks yet"
                        }
                        val emptySubtitle = when (selectedTab) {
                            PlannerFilterTab.PENDING -> if (totalCount == 0)
                                "Tap 'Add To-Do' to create your own personalized study tasks."
                            else
                                "Great job! You have completed all your pending study tasks."
                            PlannerFilterTab.COMPLETED -> "Check the box on any task once completed to track your study progress."
                            else -> "Tap 'Add To-Do' above to plan your study schedule."
                        }

                        NexoraCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("planner_empty_state")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (selectedTab == PlannerFilterTab.COMPLETED) Icons.Outlined.CheckCircle else Icons.Outlined.FormatListBulleted,
                                        contentDescription = null,
                                        tint = NexoraCyan,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = emptyTitle,
                                        color = NexoraTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = emptySubtitle,
                                        color = NexoraTextSecondary,
                                        fontSize = 12.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = {
                                            taskToEdit = null
                                            dialogInitialDate = null
                                            showAddDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NexoraCyan,
                                            contentColor = NexoraBackground
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add a Study To-Do", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(displayedTasks, key = { it.taskId }) { task ->
                        StudentTaskCard(
                            task = task,
                            onToggle = { StudyPlannerRepository.toggleTaskCompletion(task.taskId) },
                            onEdit = {
                                taskToEdit = task
                                showAddDialog = true
                            },
                            onDelete = {
                                StudyPlannerRepository.deleteTask(task.taskId)
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Add / Edit Task Dialog
    if (showAddDialog) {
        AddEditTaskDialog(
            taskToEdit = taskToEdit,
            initialDate = dialogInitialDate,
            onDismiss = {
                showAddDialog = false
                taskToEdit = null
                dialogInitialDate = null
            },
            onSave = { title, description, subjectId, scheduledDate, priority, reminderTime, reminderDateStr, reminderEpochMillis, chapterIds ->
                if (taskToEdit != null) {
                    StudyPlannerRepository.updateTask(
                        taskId = taskToEdit!!.taskId,
                        title = title,
                        description = description,
                        subjectId = subjectId,
                        scheduledDate = scheduledDate,
                        priority = priority,
                        reminderTime = reminderTime,
                        reminderDateStr = reminderDateStr,
                        reminderEpochMillis = reminderEpochMillis,
                        chapterIds = chapterIds,
                        context = context
                    )
                } else {
                    StudyPlannerRepository.addTask(
                        title = title,
                        description = description,
                        subjectId = subjectId,
                        scheduledDate = scheduledDate,
                        priority = priority,
                        reminderTime = reminderTime,
                        reminderDateStr = reminderDateStr,
                        reminderEpochMillis = reminderEpochMillis,
                        chapterIds = chapterIds,
                        context = context
                    )
                }
                showAddDialog = false
                taskToEdit = null
                dialogInitialDate = null
            },
            onDelete = if (taskToEdit != null) {
                {
                    StudyPlannerRepository.deleteTask(taskToEdit!!.taskId, context)
                    showAddDialog = false
                    taskToEdit = null
                }
            } else null
        )
    }
}

/**
 * Individual Student-Created Task Card:
 * Features:
 * - Checkbox to mark complete
 * - Title (strikethrough when completed)
 * - Description/Notes
 * - Subject badge (if assigned)
 * - Date badge (e.g. "Today", "22 Oct 2026", or "No date assigned")
 * - Priority badge
 * - Edit & Delete action buttons
 */
@Composable
private fun StudentTaskCard(
    task: StudyTask,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectColor = getSubjectColor(task.subjectId)
    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> NexoraError
        TaskPriority.MEDIUM -> NexoraGold
        TaskPriority.LOW -> NexoraCyan
    }

    NexoraCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_task_card_${task.taskId}")
            .clickable(onClick = onEdit),
        containerColor = if (task.isCompleted) NexoraSurfaceElevated.copy(alpha = 0.5f) else NexoraSurface,
        borderColor = if (task.isCompleted) NexoraSuccess.copy(alpha = 0.35f) else NexoraBorder,
        contentPadding = 14.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Top Row: Subject Tag + Date + Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (task.subjectName.isNotBlank() && task.subjectName != "General") {
                        Box(
                            modifier = Modifier
                                .background(subjectColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
                                .border(1.dp, subjectColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.subjectName,
                                color = subjectColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(priorityColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
                            .border(1.dp, priorityColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.priority.label,
                            color = priorityColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Date & Reminder Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (task.hasReminder) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier
                                .background(NexoraCyan.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .border(1.dp, NexoraCyan.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Reminder",
                                tint = NexoraCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = task.reminderFormatted.ifBlank { "Reminder" },
                                color = NexoraCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = if (task.hasDate) NexoraCyan else NexoraTextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (task.hasDate) task.scheduledDateFormatted else "No date",
                            color = if (task.hasDate) NexoraTextSecondary else NexoraTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Main Row: Checkbox + Title + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.testTag("task_checkbox_${task.taskId}"),
                    colors = CheckboxDefaults.colors(
                        checkedColor = NexoraSuccess,
                        uncheckedColor = NexoraBorder,
                        checkmarkColor = NexoraBackground
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        color = if (task.isCompleted) NexoraTextMuted else NexoraTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        lineHeight = 20.sp
                    )

                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            color = NexoraTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 2,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Action icons: Edit & Delete
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("edit_task_icon_${task.taskId}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = NexoraTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_task_icon_${task.taskId}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = NexoraError.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Filter tab button with item count badge.
 */
@Composable
private fun PlannerTabButton(
    tab: PlannerFilterTab,
    label: String,
    count: Int? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) NexoraCyan.copy(alpha = 0.16f) else NexoraSurfaceElevated)
            .border(
                1.dp,
                if (isSelected) NexoraCyan else NexoraBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("planner_tab_${tab.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                color = if (isSelected) NexoraCyan else NexoraTextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )

            if (count != null && count > 0) {
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) NexoraCyan else NexoraBorder,
                            shape = CircleShape
                        )
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = count.toString(),
                        color = if (isSelected) NexoraBackground else NexoraTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
