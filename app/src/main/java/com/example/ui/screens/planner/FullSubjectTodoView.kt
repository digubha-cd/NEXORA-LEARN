package com.example.ui.screens.planner

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.ExamType
import com.example.core.model.StudyTask
import com.example.core.model.Subject
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

private fun getSubjectAccentColor(subjectId: String): Color {
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
 * Full Subject To-Do View.
 * Displays complete chapter-wise To-Do lists for all 7 Gujarat Board Class 12 Commerce subjects:
 * 1. Gujarati
 * 2. English
 * 3. SP & CC
 * 4. B.A.
 * 5. Statistics
 * 6. Elements of Accounts
 * 7. Economics
 *
 * For each subject and chapter:
 * - All applicable chapters
 * - Interactive completion checkbox
 * - Planned study date
 * - Status (Completed / Pending / Missed)
 * - Subject progress percentage
 * - Exam phase (School Exam / Board Exam)
 */
@Composable
fun FullSubjectTodoView(
    tasks: List<StudyTask>,
    onToggleTaskCompletion: (String) -> Unit,
    onSelectTask: (StudyTask) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubjectId by remember { mutableStateOf("gujarati") }
    var filterScope by remember { mutableStateOf("ALL") } // ALL, SCHOOL, BOARD, PENDING

    val activeSubject = Subject.OFFICIAL_SUBJECTS.firstOrNull { it.id == selectedSubjectId }
        ?: Subject.OFFICIAL_SUBJECTS.first()
    val accentColor = getSubjectAccentColor(activeSubject.id)

    val subjectTasks = tasks.filter { it.subjectId == activeSubject.id }
    val totalSubjectCount = subjectTasks.size
    val completedSubjectCount = subjectTasks.count { it.isCompleted }
    val progressFraction = if (totalSubjectCount > 0) completedSubjectCount.toFloat() / totalSubjectCount else 0.0f
    val progressPercent = (progressFraction * 100).toInt()

    val filteredTasks = subjectTasks.filter { task ->
        when (filterScope) {
            "SCHOOL" -> task.examPhase == ExamType.SCHOOL_EXAM
            "BOARD" -> task.examPhase == ExamType.BOARD_EXAM
            "PENDING" -> !task.isCompleted
            else -> true
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // --- 7 Subjects Selector Tabs (Horizontal Scroll) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Subject.OFFICIAL_SUBJECTS.forEach { subject ->
                val isSelected = subject.id == selectedSubjectId
                val subTasks = tasks.filter { it.subjectId == subject.id }
                val subCompleted = subTasks.count { it.isCompleted }
                val subColor = getSubjectAccentColor(subject.id)

                Box(
                    modifier = Modifier
                        .testTag("subject_todo_tab_${subject.id}")
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) subColor.copy(alpha = 0.2f) else NexoraSurface)
                        .border(
                            1.dp,
                            if (isSelected) subColor else NexoraBorder.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedSubjectId = subject.id }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = subject.name,
                            color = if (isSelected) subColor else NexoraTextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "$subCompleted/${subTasks.size} Done",
                            color = if (isSelected) subColor else NexoraTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- Active Subject Overview Card ---
        NexoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("subject_todo_overview_${activeSubject.id}")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Text(
                                text = activeSubject.name,
                                color = NexoraTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Class 12 Commerce • Complete Chapter-wise To-Do",
                            color = NexoraTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$progressPercent% DONE",
                            color = accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = accentColor,
                    trackColor = NexoraSurfaceElevated
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$completedSubjectCount of $totalSubjectCount Chapters Completed",
                        color = NexoraTextMuted,
                        fontSize = 11.sp
                    )

                    val schoolCount = subjectTasks.count { it.examPhase == ExamType.SCHOOL_EXAM }
                    Text(
                        text = "$schoolCount School Exam Chapters",
                        color = NexoraCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scope Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "ALL" to "All (${subjectTasks.size})",
                        "SCHOOL" to "School Exam",
                        "BOARD" to "Board Exam",
                        "PENDING" to "Pending"
                    ).forEach { (scopeKey, scopeLabel) ->
                        val isScopeSelected = filterScope == scopeKey
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isScopeSelected) NexoraSurfaceElevated else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isScopeSelected) accentColor.copy(alpha = 0.6f) else NexoraBorder.copy(alpha = 0.3f),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { filterScope = scopeKey }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = scopeLabel,
                                color = if (isScopeSelected) NexoraTextPrimary else NexoraTextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (isScopeSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Chapters List ---
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No chapters matching this filter.",
                    color = NexoraTextMuted,
                    fontSize = 13.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredTasks.forEach { task ->
                    ChapterTodoItem(
                        task = task,
                        accentColor = accentColor,
                        onToggle = { onToggleTaskCompletion(task.taskId) },
                        onClick = { onSelectTask(task) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Individual Chapter To-Do item inside the full subject list.
 */
@Composable
private fun ChapterTodoItem(
    task: StudyTask,
    accentColor: Color,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val statusColor = when (task.status) {
        TaskStatus.COMPLETED -> NexoraSuccess
        TaskStatus.MISSED -> NexoraError
        TaskStatus.PENDING -> NexoraCyan
    }

    NexoraCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chapter_todo_item_${task.taskId}")
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = NexoraSuccess,
                    uncheckedColor = NexoraTextMuted,
                    checkmarkColor = NexoraBackground
                ),
                modifier = Modifier.testTag("checkbox_todo_${task.taskId}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Chapter Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.chapterTitle,
                    color = if (task.isCompleted) NexoraTextMuted else NexoraTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Planned Study Date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null,
                            tint = NexoraTextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = task.scheduledDateFormatted,
                            color = NexoraTextMuted,
                            fontSize = 11.sp
                        )
                    }

                    // Duration Badge
                    Text(
                        text = "• ${task.durationFormatted}",
                        color = NexoraTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Exam Phase Badge
                    Text(
                        text = if (task.examPhase == ExamType.SCHOOL_EXAM) "• School Exam" else "• Board Exam",
                        color = if (task.examPhase == ExamType.SCHOOL_EXAM) NexoraCyan else NexoraMagenta,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status Badge
            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = task.status.label,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
