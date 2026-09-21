package com.example.ui.screens.planner

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.Chapter
import com.example.core.model.StudyTask
import com.example.core.model.Subject
import com.example.core.model.TaskPriority
import com.example.core.reminder.ReminderSlot
import com.example.core.reminder.StudyReminderManager
import com.example.core.repository.StudyPlannerRepository
import com.example.core.repository.SyllabusRepository
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
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditTaskDialog(
    taskToEdit: StudyTask? = null,
    initialDate: LocalDate? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        subjectId: String,
        scheduledDate: LocalDate?,
        priority: TaskPriority,
        reminderTime: String?,
        reminderDateStr: String?,
        reminderEpochMillis: Long?,
        chapterIds: List<String>
    ) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isEditing = taskToEdit != null
    val context = LocalContext.current

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: taskToEdit?.notes ?: "") }
    var selectedSubjectId by remember { mutableStateOf(taskToEdit?.subjectId ?: "accounts") }
    var selectedPriority by remember { mutableStateOf(taskToEdit?.priority ?: TaskPriority.MEDIUM) }

    // Chapters multi-selection
    var selectedChapterIds by remember {
        mutableStateOf(
            if (taskToEdit != null) {
                if (taskToEdit.chapterIds.isNotEmpty()) {
                    taskToEdit.chapterIds.toSet()
                } else if (taskToEdit.chapterId.isNotBlank()) {
                    setOf(taskToEdit.chapterId)
                } else {
                    emptySet()
                }
            } else {
                emptySet()
            }
        )
    }

    // Date handling: can be null ("No Date"), Today, Tomorrow, or specific date
    var selectedDate by remember {
        mutableStateOf<LocalDate?>(
            if (taskToEdit != null) {
                if (taskToEdit.scheduledDateStr.isNotBlank()) {
                    try {
                        LocalDate.parse(taskToEdit.scheduledDateStr, StudyPlannerRepository.ISO_FORMATTER)
                    } catch (e: Exception) {
                        null
                    }
                } else null
            } else {
                initialDate
            }
        )
    }

    var titleError by remember { mutableStateOf(false) }

    // Reminder state
    var hasReminder by remember { mutableStateOf(taskToEdit?.hasReminder == true) }
    var reminderEpochMillis by remember { mutableStateOf<Long?>(taskToEdit?.reminderEpochMillis) }
    var reminderDateStr by remember { mutableStateOf<String?>(taskToEdit?.reminderDateStr) }
    var reminderFormattedText by remember { mutableStateOf(taskToEdit?.reminderFormatted ?: "") }

    // Observe real syllabus completion set
    val completedChapterIds by SyllabusRepository.completedChapterIds.collectAsStateWithLifecycle()
    val availableChapters = remember(selectedSubjectId) {
        if (selectedSubjectId.isNotBlank()) {
            SyllabusRepository.getChapters(selectedSubjectId)
        } else {
            emptyList()
        }
    }
    val pendingChapters = remember(selectedSubjectId, completedChapterIds) {
        if (selectedSubjectId.isNotBlank()) {
            SyllabusRepository.getPendingChapters(selectedSubjectId, completedChapterIds)
        } else {
            emptyList()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val setReminderAtEpoch: (Long, String) -> Unit = { epochMs, label ->
        hasReminder = true
        reminderEpochMillis = epochMs
        val zdt = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault())
        reminderDateStr = zdt.toLocalDate().format(StudyPlannerRepository.ISO_FORMATTER)
        val dtf = DateTimeFormatter.ofPattern("d MMM, h:mm a", Locale.ENGLISH)
        reminderFormattedText = zdt.format(dtf)
    }

    val clearReminder: () -> Unit = {
        hasReminder = false
        reminderEpochMillis = null
        reminderDateStr = null
        reminderFormattedText = ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .testTag("add_edit_task_dialog"),
        containerColor = NexoraSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
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
                            .size(32.dp)
                            .background(NexoraCyan.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Outlined.Edit else Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = NexoraCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = if (isEditing) "Edit Study To-Do" else "Add Chapter To-Do",
                        color = NexoraTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
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
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. SUBJECT SELECTION (Exactly 7 Class 12 Commerce Subjects)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Subject *",
                            color = NexoraTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (selectedSubjectId.isNotBlank()) {
                            val sub = Subject.OFFICIAL_SUBJECTS.firstOrNull { it.id == selectedSubjectId }
                            Text(
                                text = "${pendingChapters.size} remaining chapters",
                                color = NexoraCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Subject.OFFICIAL_SUBJECTS.forEach { sub ->
                            val isSelected = selectedSubjectId == sub.id
                            val subPendingCount = SyllabusRepository.getPendingChapters(sub.id).size
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NexoraCyan else NexoraSurfaceElevated)
                                    .border(1.dp, if (isSelected) NexoraCyan else NexoraBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedSubjectId = sub.id
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("subject_chip_${sub.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = sub.name,
                                        color = if (isSelected) NexoraBackground else NexoraTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (subPendingCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isSelected) NexoraBackground.copy(alpha = 0.25f) else NexoraBorder,
                                                    CircleShape
                                                )
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "$subPendingCount",
                                                color = if (isSelected) NexoraBackground else NexoraTextMuted,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. REAL CHAPTER SELECTION (From SyllabusRepository)
                if (availableChapters.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NexoraSurfaceElevated, RoundedCornerShape(12.dp))
                            .border(1.dp, NexoraBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                    text = "Select Chapter(s)",
                                    color = NexoraTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Quick Action: Select all pending
                            if (pendingChapters.isNotEmpty()) {
                                Text(
                                    text = "Select Remaining (${pendingChapters.size})",
                                    color = NexoraCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable {
                                            val pendingIds = pendingChapters.map { it.id }.toSet()
                                            selectedChapterIds = pendingIds
                                            if (title.isBlank() && pendingChapters.isNotEmpty()) {
                                                val subjectName = Subject.OFFICIAL_SUBJECTS.firstOrNull { it.id == selectedSubjectId }?.name ?: "Study"
                                                title = "$subjectName: ${pendingChapters.first().title}"
                                            }
                                        }
                                        .padding(vertical = 2.dp)
                                )
                            }
                        }

                        // Chapters Checkbox List
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            availableChapters.forEach { chapter ->
                                val isChecked = selectedChapterIds.contains(chapter.id)
                                val isAlreadyCompleted = completedChapterIds.contains(chapter.id)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChecked) NexoraCyan.copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable {
                                            val newSet = selectedChapterIds.toMutableSet()
                                            if (isChecked) {
                                                newSet.remove(chapter.id)
                                            } else {
                                                newSet.add(chapter.id)
                                                // Auto-populate title if empty
                                                if (title.isBlank()) {
                                                    title = chapter.title
                                                }
                                            }
                                            selectedChapterIds = newSet
                                        }
                                        .padding(horizontal = 6.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            val newSet = selectedChapterIds.toMutableSet()
                                            if (checked) {
                                                newSet.add(chapter.id)
                                                if (title.isBlank()) title = chapter.title
                                            } else {
                                                newSet.remove(chapter.id)
                                            }
                                            selectedChapterIds = newSet
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = NexoraCyan,
                                            uncheckedColor = NexoraBorder,
                                            checkmarkColor = NexoraBackground
                                        ),
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = chapter.title,
                                            color = if (isAlreadyCompleted) NexoraTextMuted else NexoraTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                            lineHeight = 16.sp
                                        )
                                    }

                                    // Chapter Points Badge
                                    Box(
                                        modifier = Modifier
                                            .background(NexoraGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "+${chapter.points} pts",
                                            color = NexoraGold,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (isAlreadyCompleted) {
                                        Box(
                                            modifier = Modifier
                                                .background(NexoraSuccess.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Done",
                                                color = NexoraSuccess,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. TASK TITLE FIELD
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Task Title *",
                        color = NexoraTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (titleError && it.isNotBlank()) titleError = false
                        },
                        placeholder = {
                            Text(
                                text = "e.g. Accounts Ch 1 revision & practical sums",
                                color = NexoraTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_title_input"),
                        singleLine = true,
                        isError = titleError,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexoraCyan,
                            unfocusedBorderColor = NexoraBorder,
                            focusedTextColor = NexoraTextPrimary,
                            unfocusedTextColor = NexoraTextPrimary,
                            errorBorderColor = NexoraError
                        )
                    )
                    if (titleError) {
                        Text(
                            text = "Please enter a task title",
                            color = NexoraError,
                            fontSize = 11.sp
                        )
                    }
                }

                // 4. DESCRIPTION / NOTES FIELD (Optional)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Notes / Concepts (Optional)",
                        color = NexoraTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = {
                            Text(
                                text = "Key formulas, specific question numbers, revision targets...",
                                color = NexoraTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .testTag("task_description_input"),
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexoraCyan,
                            unfocusedBorderColor = NexoraBorder,
                            focusedTextColor = NexoraTextPrimary,
                            unfocusedTextColor = NexoraTextPrimary
                        )
                    )
                }

                // 5. DATE SELECTION (Optional)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Assign Date (Optional)",
                            color = NexoraTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = selectedDate?.format(StudyPlannerRepository.DATE_FORMATTER) ?: "No date assigned",
                            color = if (selectedDate != null) NexoraCyan else NexoraTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val today = StudyPlannerRepository.TODAY
                        val tomorrow = today.plusDays(1)

                        DateOptionChip(
                            label = "No Date",
                            isSelected = selectedDate == null,
                            onClick = { selectedDate = null }
                        )

                        DateOptionChip(
                            label = "Today",
                            isSelected = selectedDate == today,
                            onClick = { selectedDate = today }
                        )

                        DateOptionChip(
                            label = "Tomorrow",
                            isSelected = selectedDate == tomorrow,
                            onClick = { selectedDate = tomorrow }
                        )

                        DateOptionChip(
                            label = "School Exam (22 Oct)",
                            isSelected = selectedDate == StudyPlannerRepository.SCHOOL_EXAM_DATE,
                            onClick = { selectedDate = StudyPlannerRepository.SCHOOL_EXAM_DATE }
                        )

                        DateOptionChip(
                            label = "Board Exam (25 Feb)",
                            isSelected = selectedDate == StudyPlannerRepository.BOARD_EXAM_DATE,
                            onClick = { selectedDate = StudyPlannerRepository.BOARD_EXAM_DATE }
                        )
                    }
                }

                // 6. PRIORITY SELECTION
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Priority",
                        color = NexoraTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TaskPriority.values().forEach { priority ->
                            val isSelected = selectedPriority == priority
                            val color = when (priority) {
                                TaskPriority.HIGH -> NexoraError
                                TaskPriority.MEDIUM -> NexoraGold
                                TaskPriority.LOW -> NexoraCyan
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.2f) else NexoraSurfaceElevated)
                                    .border(1.dp, if (isSelected) color else NexoraBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedPriority = priority }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = priority.label,
                                    color = if (isSelected) color else NexoraTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // 7. STUDY REMINDER & REMAINING CHAPTERS REMINDERS (Morning, Evening, Night)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NexoraSurfaceElevated, RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            if (hasReminder) NexoraCyan.copy(alpha = 0.5f) else NexoraBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Icon(
                                imageVector = if (hasReminder) Icons.Filled.NotificationsActive else Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = if (hasReminder) NexoraCyan else NexoraTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Study Reminder",
                                    color = NexoraTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Custom notification or Morning/Evening/Night slots",
                                    color = NexoraTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Switch(
                            checked = hasReminder,
                            onCheckedChange = { enabled ->
                                hasReminder = enabled
                                if (enabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    if (reminderEpochMillis == null) {
                                        setReminderAtEpoch(System.currentTimeMillis() + 60 * 60 * 1000L, "In 1h")
                                    }
                                } else {
                                    clearReminder()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NexoraBackground,
                                checkedTrackColor = NexoraCyan,
                                uncheckedThumbColor = NexoraTextMuted,
                                uncheckedTrackColor = NexoraSurface
                            ),
                            modifier = Modifier.testTag("reminder_toggle_switch")
                        )
                    }

                    if (hasReminder) {
                        if (reminderFormattedText.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(NexoraCyan.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                    .border(1.dp, NexoraCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Alarm,
                                        contentDescription = null,
                                        tint = NexoraCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = reminderFormattedText,
                                        color = NexoraCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                IconButton(
                                    onClick = clearReminder,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Remove Reminder",
                                        tint = NexoraTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Time Preset Slots including Morning, Evening, Night
                        Text(
                            text = "Remaining Chapters & Study Slots",
                            color = NexoraTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val targetDate = selectedDate ?: LocalDate.now()

                            // Morning (7:00 AM)
                            ReminderPresetChip(
                                label = "Morning (7 AM)",
                                onClick = {
                                    val epoch = targetDate.atTime(7, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                    val finalEpoch = if (epoch < System.currentTimeMillis()) epoch + 24 * 60 * 60 * 1000L else epoch
                                    setReminderAtEpoch(finalEpoch, "Morning 7:00 AM")
                                    if (selectedSubjectId.isNotBlank()) {
                                        StudyReminderManager.scheduleRemainingChaptersReminder(context, selectedSubjectId, ReminderSlot.MORNING, targetDate)
                                    }
                                }
                            )

                            // Evening (5:00 PM)
                            ReminderPresetChip(
                                label = "Evening (5 PM)",
                                onClick = {
                                    val epoch = targetDate.atTime(17, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                    val finalEpoch = if (epoch < System.currentTimeMillis()) epoch + 24 * 60 * 60 * 1000L else epoch
                                    setReminderAtEpoch(finalEpoch, "Evening 5:00 PM")
                                    if (selectedSubjectId.isNotBlank()) {
                                        StudyReminderManager.scheduleRemainingChaptersReminder(context, selectedSubjectId, ReminderSlot.EVENING, targetDate)
                                    }
                                }
                            )

                            // Night (9:00 PM)
                            ReminderPresetChip(
                                label = "Night (9 PM)",
                                onClick = {
                                    val epoch = targetDate.atTime(21, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                    val finalEpoch = if (epoch < System.currentTimeMillis()) epoch + 24 * 60 * 60 * 1000L else epoch
                                    setReminderAtEpoch(finalEpoch, "Night 9:00 PM")
                                    if (selectedSubjectId.isNotBlank()) {
                                        StudyReminderManager.scheduleRemainingChaptersReminder(context, selectedSubjectId, ReminderSlot.NIGHT, targetDate)
                                    }
                                }
                            )

                            ReminderPresetChip(
                                label = "In 1 Hour",
                                onClick = {
                                    setReminderAtEpoch(System.currentTimeMillis() + 60 * 60 * 1000L, "In 1h")
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onSave(
                            title,
                            description,
                            selectedSubjectId,
                            selectedDate,
                            selectedPriority,
                            if (hasReminder) reminderFormattedText.ifBlank { "Reminder Set" } else null,
                            if (hasReminder) reminderDateStr else null,
                            if (hasReminder) reminderEpochMillis else null,
                            selectedChapterIds.toList()
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NexoraCyan,
                    contentColor = NexoraBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_task_button")
            ) {
                Text(
                    text = if (isEditing) "Update To-Do" else "Add Chapter To-Do",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isEditing && onDelete != null) {
                    TextButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = NexoraError),
                        modifier = Modifier.testTag("delete_task_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Delete", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = NexoraTextSecondary)
                ) {
                    Text(text = "Cancel", fontSize = 13.sp)
                }
            }
        }
    )
}

@Composable
private fun DateOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NexoraCyan else NexoraSurfaceElevated)
            .border(1.dp, if (isSelected) NexoraCyan else NexoraBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) NexoraBackground else NexoraTextPrimary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ReminderPresetChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NexoraSurface)
            .border(1.dp, NexoraBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = NexoraCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
