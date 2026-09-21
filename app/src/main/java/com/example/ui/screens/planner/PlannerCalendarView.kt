package com.example.ui.screens.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Timer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.StudyTask
import com.example.core.model.TaskPriority
import com.example.core.model.TaskStatus
import com.example.core.repository.StudyPlannerRepository
import com.example.ui.components.NexoraCard
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

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
 * Calendar View for Study Planner:
 * Shows ONLY:
 * - Student-created To-Do tasks
 * - Completed tasks
 * - Selected date
 * - Exam dates (School Exam: 22 Oct 2026, Board Exam: 25 Feb 2027)
 *
 * Does NOT automatically populate study tasks into the calendar.
 */
@Composable
fun PlannerCalendarView(
    tasks: List<StudyTask>,
    onToggleTaskCompletion: (String) -> Unit,
    onSelectTask: (StudyTask) -> Unit,
    onAddTaskForDate: ((LocalDate) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var currentYearMonth by remember {
        val today = StudyPlannerRepository.TODAY
        mutableStateOf(YearMonth.of(today.year, today.month))
    }
    var selectedDate by remember { mutableStateOf(StudyPlannerRepository.TODAY) }

    val today = StudyPlannerRepository.TODAY
    val selectedDateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

    // Student-created tasks scheduled on this selected date
    val selectedDateTasks = tasks.filter { it.scheduledDateStr == selectedDateStr }

    // Dynamic exam countdowns calculated for the selected date
    val schoolDays = StudyPlannerRepository.getSchoolExamDaysRemaining(selectedDate)
    val boardDays = StudyPlannerRepository.getBoardExamDaysRemaining(selectedDate)
    val schoolCountdownText = StudyPlannerRepository.formatExamCountdown(schoolDays)
    val boardCountdownText = StudyPlannerRepository.formatExamCountdown(boardDays)

    val isExamMilestone = selectedDateStr == StudyPlannerRepository.SCHOOL_EXAM_DATE_STR ||
            selectedDateStr == StudyPlannerRepository.BOARD_EXAM_DATE_STR
    val milestoneTitle = StudyPlannerRepository.getMilestoneOnDate(selectedDateStr)

    val completedCount = selectedDateTasks.count { it.isCompleted }
    val totalCount = selectedDateTasks.size
    val planProgress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("planner_calendar_view")
    ) {
        // --- Month Navigator Header ---
        NexoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("calendar_prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Previous Month",
                            tint = NexoraCyan
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${currentYearMonth.year}",
                            color = NexoraTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("calendar_month_label")
                        )
                        Text(
                            text = "Class 12 Commerce Academic Year",
                            color = NexoraTextMuted,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("calendar_next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = "Next Month",
                            tint = NexoraCyan
                        )
                    }
                }

                // Quick jump to Today or Exam Months
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            currentYearMonth = YearMonth.of(today.year, today.month)
                            selectedDate = today
                        },
                        modifier = Modifier.testTag("calendar_today_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null,
                            tint = NexoraCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Today (${today.format(StudyPlannerRepository.DATE_FORMATTER)})",
                            color = NexoraCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            currentYearMonth = YearMonth.of(2026, 10)
                            selectedDate = StudyPlannerRepository.SCHOOL_EXAM_DATE
                        },
                        modifier = Modifier.testTag("calendar_school_exam_jump_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = null,
                            tint = NexoraGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "22 Oct (School Exam)",
                            color = NexoraGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Day-of-week Headers ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY,
                        DayOfWeek.SATURDAY,
                        DayOfWeek.SUNDAY
                    ).forEach { day ->
                        val dayName = day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).take(2)
                        Text(
                            text = dayName,
                            color = if (day == DayOfWeek.SUNDAY) NexoraGold else NexoraTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // --- Calendar Days Grid ---
                val firstDayOfMonth = currentYearMonth.atDay(1)
                val daysInMonth = currentYearMonth.lengthOfMonth()
                val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value - 1) // 0 for Monday

                val totalCells = ((dayOfWeekOffset + daysInMonth + 6) / 7) * 7

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (row in 0 until (totalCells / 7)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                val dayNumber = cellIndex - dayOfWeekOffset + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val cellDate = currentYearMonth.atDay(dayNumber)
                                    val cellDateStr = cellDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

                                    val isSelected = cellDate == selectedDate
                                    val isToday = cellDate == today

                                    val isSchoolExam = cellDateStr == StudyPlannerRepository.SCHOOL_EXAM_DATE_STR
                                    val isBoardExam = cellDateStr == StudyPlannerRepository.BOARD_EXAM_DATE_STR
                                    val isExamDay = isSchoolExam || isBoardExam

                                    // Student task indicators on this date
                                    val tasksForCell = tasks.filter { it.scheduledDateStr == cellDateStr }
                                    val hasCompleted = tasksForCell.any { it.isCompleted }
                                    val hasPending = tasksForCell.any { !it.isCompleted }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isSelected -> NexoraCyan.copy(alpha = 0.25f)
                                                    isExamDay -> NexoraGold.copy(alpha = 0.15f)
                                                    isToday -> NexoraSurfaceElevated
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = when {
                                                    isSelected -> 1.5.dp
                                                    isExamDay -> 1.dp
                                                    isToday -> 1.dp
                                                    else -> 0.dp
                                                },
                                                color = when {
                                                    isSelected -> NexoraCyan
                                                    isExamDay -> NexoraGold
                                                    isToday -> NexoraBorder
                                                    else -> Color.Transparent
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedDate = cellDate }
                                            .testTag("calendar_day_${cellDateStr}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$dayNumber",
                                                color = when {
                                                    isSelected -> NexoraCyan
                                                    isExamDay -> NexoraGold
                                                    isToday -> NexoraCyan
                                                    cellDate.dayOfWeek == DayOfWeek.SUNDAY -> NexoraGold.copy(alpha = 0.8f)
                                                    else -> NexoraTextPrimary
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected || isToday || isExamDay) FontWeight.Bold else FontWeight.Normal
                                            )

                                            // Status dots showing ONLY student tasks and exam dates
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.height(6.dp)
                                            ) {
                                                if (isExamDay) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(NexoraGold)
                                                    )
                                                }
                                                if (hasCompleted) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(NexoraSuccess)
                                                    )
                                                }
                                                if (hasPending) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(NexoraCyan)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Empty padding box for days outside month
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Legend
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(color = NexoraGold, label = "Exam Dates")
                    LegendItem(color = NexoraCyan, label = "Pending Task")
                    LegendItem(color = NexoraSuccess, label = "Completed Task")
                }
            }
        }

        // --- Dynamic Milestone Banner if Selected Date is an Exam Milestone ---
        if (isExamMilestone && milestoneTitle != null) {
            NexoraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                borderColor = NexoraGold,
                containerColor = NexoraGold.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(NexoraGold.copy(alpha = 0.2f))
                            .border(1.dp, NexoraGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = null,
                            tint = NexoraGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = milestoneTitle,
                            color = NexoraGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedDateStr == StudyPlannerRepository.SCHOOL_EXAM_DATE_STR)
                                "Target: 90+ Marks in School Exam (Phase 1)"
                            else
                                "Target: 90+ Marks in Final Board Exam (Phase 2)",
                            color = NexoraTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // --- Dynamic Exam Countdown Banner ---
        NexoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // School Exam Countdown
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
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
                            color = NexoraTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "22 Oct 2026",
                        color = NexoraTextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = schoolCountdownText,
                        color = if (schoolDays <= 0L) NexoraGold else NexoraCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("calendar_school_exam_countdown")
                    )
                }

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(NexoraBorder)
                )

                // Board Exam Countdown
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
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
                            color = NexoraTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "25 Feb 2027",
                        color = NexoraTextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = boardCountdownText,
                        color = if (boardDays <= 0L) NexoraGold else NexoraGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("calendar_board_exam_countdown")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- Selected Date Header with Add To-Do action ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (selectedDate == today) "Today's To-Do (${selectedDate.format(StudyPlannerRepository.DATE_FORMATTER)})"
                    else "To-Do for ${selectedDate.format(StudyPlannerRepository.DATE_FORMATTER)}",
                    color = NexoraTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("calendar_selected_date_title")
                )
                Text(
                    text = if (totalCount > 0) "$completedCount of $totalCount completed" else "No tasks assigned",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp
                )
            }

            if (onAddTaskForDate != null) {
                Button(
                    onClick = { onAddTaskForDate(selectedDate) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NexoraCyan,
                        contentColor = NexoraBackground
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("calendar_add_task_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add To-Do",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add To-Do",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (totalCount > 0) {
            LinearProgressIndicator(
                progress = { planProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NexoraCyan,
                trackColor = NexoraSurfaceElevated
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Student-created To-Do Tasks for Selected Date ---
        if (selectedDateTasks.isEmpty()) {
            NexoraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = null,
                            tint = NexoraTextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No study tasks scheduled on this date.",
                            color = NexoraTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (onAddTaskForDate != null) {
                            Button(
                                onClick = { onAddTaskForDate(selectedDate) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NexoraCyan,
                                    contentColor = NexoraBackground
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("calendar_empty_add_task_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add To-Do for this Date", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedDateTasks.forEach { task ->
                    CalendarTaskItem(
                        task = task,
                        onToggle = { onToggleTaskCompletion(task.taskId) },
                        onClick = { onSelectTask(task) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CalendarTaskItem(
    task: StudyTask,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val subjectColor = getSubjectColor(task.subjectId)
    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> NexoraError
        TaskPriority.MEDIUM -> NexoraGold
        TaskPriority.LOW -> NexoraCyan
    }

    NexoraCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("calendar_task_item_${task.taskId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = NexoraSuccess,
                    uncheckedColor = NexoraTextMuted,
                    checkmarkColor = NexoraBackground
                ),
                modifier = Modifier.testTag("calendar_checkbox_${task.taskId}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (task.subjectName.isNotBlank() && task.subjectName != "General") {
                        Box(
                            modifier = Modifier
                                .background(subjectColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
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
                            .background(priorityColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.priority.label,
                            color = priorityColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.title,
                    color = if (task.isCompleted) NexoraTextMuted else NexoraTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        color = NexoraTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Completed / Pending Indicator
            Box(
                modifier = Modifier
                    .background(
                        if (task.isCompleted) NexoraSuccess.copy(alpha = 0.15f) else NexoraCyan.copy(alpha = 0.15f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (task.isCompleted) "Completed" else "Pending",
                    color = if (task.isCompleted) NexoraSuccess else NexoraCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            color = NexoraTextMuted,
            fontSize = 10.sp
        )
    }
}
