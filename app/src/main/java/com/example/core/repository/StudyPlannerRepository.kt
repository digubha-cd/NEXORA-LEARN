package com.example.core.repository

import android.content.Context
import com.example.core.model.DailyStudyPlan
import com.example.core.model.ExamType
import com.example.core.model.StudyProgressSummary
import com.example.core.model.StudyStreak
import com.example.core.model.StudyTask
import com.example.core.model.Subject
import com.example.core.model.SubjectProgressItem
import com.example.core.model.TaskPriority
import com.example.core.reminder.StudyReminderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

/**
 * Study Planner Repository:
 * Manages Student-Created To-Do tasks and automatic Exam Countdowns.
 *
 * Requirements:
 * 1. NO automatic task generation. The student creates their own tasks.
 * 2. Allow students to:
 *    - Add a To-Do
 *    - Edit a To-Do
 *    - Delete a To-Do
 *    - Mark Complete with a checkbox
 *    - See Pending and Completed tasks
 *    - Optionally assign a date to a task
 * 3. Keep existing date-based Calendar, showing ONLY student-created tasks, completed tasks,
 *    selected date, and exam dates (no auto-population).
 * 4. Keep exam countdowns completely automatic:
 *    - School Exam — 22 October 2026
 *    - Board Exam — 25 February 2027
 *    - Calculate remaining days from actual current date.
 *    - Automatically decreases by exactly 1 day each calendar day.
 *    - On the exam date show: EXAM DAY
 *    - Never show negative countdown numbers.
 */
object StudyPlannerRepository {

    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
    val ISO_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // Configurable/testable reference date; defaults dynamically to LocalDate.now()
    var currentDateOverride: LocalDate? = null
    val TODAY: LocalDate get() = currentDateOverride ?: LocalDate.now()
    val TODAY_STR: String get() = TODAY.format(ISO_FORMATTER)
    val TODAY_FORMATTED: String get() = TODAY.format(DATE_FORMATTER)

    // Milestone Exam Dates
    val SCHOOL_EXAM_DATE: LocalDate = LocalDate.of(2026, 10, 22)
    val SCHOOL_EXAM_DATE_STR: String = SCHOOL_EXAM_DATE.format(ISO_FORMATTER)
    val SCHOOL_EXAM_FORMATTED: String = SCHOOL_EXAM_DATE.format(DATE_FORMATTER)

    val BOARD_EXAM_DATE: LocalDate = LocalDate.of(2027, 2, 25)
    val BOARD_EXAM_DATE_STR: String = BOARD_EXAM_DATE.format(ISO_FORMATTER)
    val BOARD_EXAM_FORMATTED: String = BOARD_EXAM_DATE.format(DATE_FORMATTER)

    // Standard academic ordering for the 7 Class 12 Commerce subjects
    val ORDERED_SUBJECT_IDS: List<String> = listOf(
        "accounts",
        "stat",
        "economics",
        "ba",
        "sp_cc",
        "english",
        "gujarati"
    )

    // Student-created tasks list. Starts empty - NO automatic generation!
    private val _tasks = MutableStateFlow<List<StudyTask>>(emptyList())
    val tasks: StateFlow<List<StudyTask>> = _tasks.asStateFlow()

    init {
        // Listen to syllabus chapter completion events to synchronize if a student-created task references a chapter
        SyllabusRepository.onChapterCompletionChanged = { chapterId, isCompleted ->
            syncFromChapter(chapterId, isCompleted)
        }
    }

    /**
     * Study duration distribution recommendation for the 7 subjects.
     */
    fun getSubjectDailyDuration(subjectId: String): Int {
        return when (subjectId) {
            "accounts" -> 50
            "stat" -> 45
            "economics" -> 45
            "ba" -> 45
            "sp_cc" -> 40
            "english" -> 40
            "gujarati" -> 35
            else -> 30
        }
    }

    // ==========================================
    // EXAM COUNTDOWN ENGINE
    // ==========================================

    /**
     * Dynamically calculates days remaining until School Exam (22 October 2026).
     * Never returns negative numbers (clamped to 0).
     */
    fun getSchoolExamDaysRemaining(from: LocalDate = TODAY): Long {
        val days = ChronoUnit.DAYS.between(from, SCHOOL_EXAM_DATE)
        return maxOf(0L, days)
    }

    /**
     * Dynamically calculates days remaining until Board Exam (25 February 2027).
     * Never returns negative numbers (clamped to 0).
     */
    fun getBoardExamDaysRemaining(from: LocalDate = TODAY): Long {
        val days = ChronoUnit.DAYS.between(from, BOARD_EXAM_DATE)
        return maxOf(0L, days)
    }

    /**
     * Formats days remaining label.
     * On exam day: "EXAM DAY"
     * For 1 day: "1 day"
     * For > 1: "$days days"
     * Never shows negative numbers.
     */
    fun formatDays(days: Long): String {
        return when {
            days <= 0L -> "EXAM DAY"
            days == 1L -> "1 day"
            else -> "$days days"
        }
    }

    /**
     * Format exam countdown with optional exam name prefix:
     * e.g., "School Exam: 32 days" or "School Exam — EXAM DAY"
     */
    fun formatExamCountdown(days: Long, examName: String? = null): String {
        val countText = formatDays(days)
        return if (examName != null) {
            if (countText == "EXAM DAY") "$examName — EXAM DAY" else "$examName: $countText"
        } else {
            countText
        }
    }

    // ==========================================
    // STUDENT-CREATED TO-DO CRUD OPERATIONS
    // ==========================================

    /**
     * Student adds a new To-Do task.
     * Date is optional.
     */
    fun addTask(
        title: String,
        description: String = "",
        subjectId: String = "",
        scheduledDate: LocalDate? = null,
        priority: TaskPriority = TaskPriority.MEDIUM,
        durationMinutes: Int = 30,
        notes: String = "",
        reminderTime: String? = null,
        chapterId: String = "",
        chapterIds: List<String> = emptyList(),
        examPhase: ExamType? = null,
        reminderDateStr: String? = null,
        reminderEpochMillis: Long? = null,
        context: Context? = null
    ): StudyTask {
        val dateStr = scheduledDate?.format(ISO_FORMATTER) ?: ""
        val dateFormatted = scheduledDate?.format(DATE_FORMATTER) ?: "No date assigned"
        val subject = Subject.OFFICIAL_SUBJECTS.firstOrNull { it.id == subjectId }
        val subjectName = subject?.name ?: if (subjectId.isNotBlank()) subjectId else "General"
        val resolvedPhase = examPhase ?: if (scheduledDate != null) getActiveExamPhase(scheduledDate) else ExamType.SCHOOL_EXAM

        val finalChapterIds = if (chapterIds.isNotEmpty()) {
            chapterIds
        } else if (chapterId.isNotBlank()) {
            listOf(chapterId)
        } else {
            emptyList()
        }
        val finalChapterId = if (chapterId.isNotBlank()) chapterId else finalChapterIds.firstOrNull() ?: ""

        val newTask = StudyTask(
            taskId = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            subjectId = subjectId,
            subjectName = subjectName,
            chapterId = finalChapterId,
            chapterTitle = title.trim(),
            chapterIds = finalChapterIds,
            examPhase = resolvedPhase,
            scheduledDateStr = dateStr,
            scheduledDateFormatted = dateFormatted,
            isCompleted = false,
            priority = priority,
            durationMinutes = durationMinutes,
            notes = notes.trim(),
            reminderTime = reminderTime,
            reminderDateStr = reminderDateStr,
            reminderEpochMillis = reminderEpochMillis,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        _tasks.value = _tasks.value + newTask

        // If a reminder was set and context is provided, schedule the Android notification alarm
        if (context != null && reminderEpochMillis != null) {
            StudyReminderManager.scheduleReminder(context, newTask, reminderEpochMillis)
        }

        return newTask
    }

    /**
     * Student edits an existing To-Do task.
     * Also updates or cancels any scheduled reminder.
     */
    fun updateTask(
        taskId: String,
        title: String,
        description: String = "",
        subjectId: String = "",
        scheduledDate: LocalDate? = null,
        priority: TaskPriority = TaskPriority.MEDIUM,
        durationMinutes: Int = 30,
        notes: String = "",
        reminderTime: String? = null,
        reminderDateStr: String? = null,
        reminderEpochMillis: Long? = null,
        chapterIds: List<String>? = null,
        context: Context? = null
    ) {
        val dateStr = scheduledDate?.format(ISO_FORMATTER) ?: ""
        val dateFormatted = scheduledDate?.format(DATE_FORMATTER) ?: "No date assigned"
        val subject = Subject.OFFICIAL_SUBJECTS.firstOrNull { it.id == subjectId }
        val subjectName = subject?.name ?: if (subjectId.isNotBlank()) subjectId else "General"

        var updatedTask: StudyTask? = null

        _tasks.value = _tasks.value.map { task ->
            if (task.taskId == taskId) {
                val newChapterIds = chapterIds ?: task.chapterIds
                val updated = task.copy(
                    title = title.trim(),
                    description = description.trim(),
                    subjectId = subjectId,
                    subjectName = subjectName,
                    chapterIds = newChapterIds,
                    chapterId = if (newChapterIds.isNotEmpty()) newChapterIds.first() else task.chapterId,
                    scheduledDateStr = dateStr,
                    scheduledDateFormatted = dateFormatted,
                    priority = priority,
                    durationMinutes = durationMinutes,
                    notes = notes.trim(),
                    reminderTime = reminderTime,
                    reminderDateStr = reminderDateStr,
                    reminderEpochMillis = reminderEpochMillis,
                    updatedAt = System.currentTimeMillis()
                )
                updatedTask = updated
                updated
            } else {
                task
            }
        }

        if (context != null) {
            if (reminderEpochMillis != null && updatedTask != null) {
                StudyReminderManager.scheduleReminder(context, updatedTask!!, reminderEpochMillis)
            } else if (reminderEpochMillis == null && reminderTime.isNullOrBlank()) {
                StudyReminderManager.cancelReminder(context, taskId)
            }
        }
    }

    /**
     * Student deletes a To-Do task.
     * Also cancels any active alarm reminder.
     */
    fun deleteTask(taskId: String, context: Context? = null) {
        context?.let { StudyReminderManager.cancelReminder(it, taskId) }
        _tasks.value = _tasks.value.filterNot { it.taskId == taskId }
    }

    /**
     * Clear reminder from a task without deleting the task.
     */
    fun deleteReminder(taskId: String, context: Context? = null) {
        context?.let { StudyReminderManager.cancelReminder(it, taskId) }
        _tasks.value = _tasks.value.map { task ->
            if (task.taskId == taskId) {
                task.copy(
                    reminderTime = null,
                    reminderDateStr = null,
                    reminderEpochMillis = null,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                task
            }
        }
    }

    /**
     * Toggle completion of a study task with a checkbox.
     * When completed, marks all linked chapters as completed in SyllabusRepository,
     * updating subject progress, profile progress, chapter points, and removing them from remaining reminders.
     */
    fun toggleTaskCompletion(taskId: String) {
        val currentList = _tasks.value.toMutableList()
        val index = currentList.indexOfFirst { it.taskId == taskId }
        if (index != -1) {
            val task = currentList[index]
            val newCompleted = !task.isCompleted
            val updated = task.copy(
                isCompleted = newCompleted,
                completedAt = if (newCompleted) System.currentTimeMillis() else null,
                isMissed = if (newCompleted) false else task.isMissed,
                updatedAt = System.currentTimeMillis()
            )
            currentList[index] = updated
            _tasks.value = currentList

            // If this task is associated with chapter(s), sync to syllabus
            val chaptersToSync = if (task.chapterIds.isNotEmpty()) {
                task.chapterIds
            } else if (task.chapterId.isNotBlank()) {
                listOf(task.chapterId)
            } else {
                emptyList()
            }

            chaptersToSync.forEach { chId ->
                SyllabusRepository.setChapterCompleted(chId, newCompleted)
            }
        }
    }

    /**
     * Reschedule a task to a target date.
     */
    fun rescheduleTask(taskId: String, targetDate: LocalDate = TODAY) {
        val currentList = _tasks.value.toMutableList()
        val index = currentList.indexOfFirst { it.taskId == taskId }
        if (index != -1) {
            val task = currentList[index]
            val updated = task.copy(
                scheduledDateStr = targetDate.format(ISO_FORMATTER),
                scheduledDateFormatted = targetDate.format(DATE_FORMATTER),
                isMissed = false,
                rescheduled = true,
                updatedAt = System.currentTimeMillis()
            )
            currentList[index] = updated
            _tasks.value = currentList
        }
    }

    /**
     * Reschedule all currently missed tasks to today.
     */
    fun rescheduleAllMissedToToday() {
        val currentList = _tasks.value.toMutableList()
        var modified = false
        currentList.forEachIndexed { index, task ->
            if (task.isMissed && !task.isCompleted) {
                currentList[index] = task.copy(
                    scheduledDateStr = TODAY_STR,
                    scheduledDateFormatted = TODAY_FORMATTED,
                    isMissed = false,
                    rescheduled = true,
                    updatedAt = System.currentTimeMillis()
                )
                modified = true
            }
        }
        if (modified) {
            _tasks.value = currentList
        }
    }

    /**
     * Synchronize completion when user checks a chapter checkbox directly in SubjectDetailScreen.
     */
    private fun syncFromChapter(chapterId: String, isCompleted: Boolean) {
        val currentList = _tasks.value.toMutableList()
        var modified = false
        currentList.forEachIndexed { index, task ->
            val matches = task.chapterId == chapterId || task.chapterIds.contains(chapterId)
            if (matches && task.isCompleted != isCompleted) {
                currentList[index] = task.copy(
                    isCompleted = isCompleted,
                    completedAt = if (isCompleted) System.currentTimeMillis() else null,
                    isMissed = if (isCompleted) false else task.isMissed,
                    updatedAt = System.currentTimeMillis()
                )
                modified = true
            }
        }
        if (modified) {
            _tasks.value = currentList
        }
    }

    /**
     * Mark or unmark a task as missed.
     */
    fun setTaskMissed(taskId: String, isMissed: Boolean) {
        val currentList = _tasks.value.toMutableList()
        val index = currentList.indexOfFirst { it.taskId == taskId }
        if (index != -1) {
            val task = currentList[index]
            val updated = task.copy(
                isMissed = isMissed,
                isCompleted = if (isMissed) false else task.isCompleted,
                updatedAt = System.currentTimeMillis()
            )
            currentList[index] = updated
            _tasks.value = currentList
            if (isMissed && task.chapterId.isNotBlank()) {
                SyllabusRepository.setChapterCompleted(task.chapterId, false)
            }
        }
    }

    // ==========================================
    // QUERY FILTERS FOR STUDENT TO-DO
    // ==========================================

    fun getPendingTasks(): List<StudyTask> {
        return _tasks.value.filter { !it.isCompleted }
    }

    fun getCompletedTasks(): List<StudyTask> {
        return _tasks.value.filter { it.isCompleted }
    }

    fun getTodayTasks(): List<StudyTask> {
        val todayStr = TODAY_STR
        return _tasks.value.filter { it.scheduledDateStr == todayStr }
    }

    fun getUpcomingTasks(): List<StudyTask> {
        val today = TODAY
        return _tasks.value.filter { task ->
            if (task.scheduledDateStr.isBlank() || task.isCompleted) false
            else {
                try {
                    val date = LocalDate.parse(task.scheduledDateStr, ISO_FORMATTER)
                    date.isAfter(today)
                } catch (e: Exception) {
                    false
                }
            }
        }.sortedBy { it.scheduledDateStr }
    }

    fun getMissedTasks(): List<StudyTask> {
        val today = TODAY
        return _tasks.value.filter { task ->
            if (task.scheduledDateStr.isBlank() || task.isCompleted) false
            else {
                try {
                    val date = LocalDate.parse(task.scheduledDateStr, ISO_FORMATTER)
                    date.isBefore(today)
                } catch (e: Exception) {
                    false
                }
            }
        }
    }

    fun getTasksWithoutDate(): List<StudyTask> {
        return _tasks.value.filter { it.scheduledDateStr.isBlank() }
    }

    fun getTasksForDate(dateStr: String): List<StudyTask> {
        return _tasks.value.filter { it.scheduledDateStr == dateStr }
    }

    fun hasTasksOnDate(dateStr: String): Boolean {
        return _tasks.value.any { it.scheduledDateStr == dateStr }
    }

    fun hasCompletedOnDate(dateStr: String): Boolean {
        return _tasks.value.any { it.scheduledDateStr == dateStr && it.isCompleted }
    }

    fun hasMissedOnDate(dateStr: String): Boolean {
        return _tasks.value.any { it.scheduledDateStr == dateStr && it.isMissed && !it.isCompleted }
    }

    fun hasUpcomingOnDate(dateStr: String): Boolean {
        return _tasks.value.any { it.scheduledDateStr == dateStr && !it.isCompleted && !it.isMissed }
    }

    fun getMilestoneOnDate(dateStr: String): String? {
        return when (dateStr) {
            SCHOOL_EXAM_DATE_STR -> "School Exam — 22 October 2026"
            BOARD_EXAM_DATE_STR -> "Board Exam — 25 February 2027"
            else -> null
        }
    }

    /**
     * Returns student tasks for the given calendar date with exam countdowns.
     * Does NOT generate automatic study tasks.
     */
    fun getDailyStudyPlan(date: LocalDate): DailyStudyPlan {
        val dateStr = date.format(ISO_FORMATTER)
        val dateFormatted = date.format(DATE_FORMATTER)
        val dayTasks = getTasksForDate(dateStr)
        val schoolRemaining = getSchoolExamDaysRemaining(date)
        val boardRemaining = getBoardExamDaysRemaining(date)
        val totalMins = dayTasks.sumOf { it.durationMinutes }

        return DailyStudyPlan(
            dateStr = dateStr,
            dateFormatted = dateFormatted,
            totalHours = totalMins / 60,
            totalMinutes = totalMins,
            tasks = dayTasks,
            schoolExamDaysRemaining = schoolRemaining,
            boardExamDaysRemaining = boardRemaining
        )
    }

    fun getActiveExamPhase(date: LocalDate = TODAY): ExamType {
        return if (date.isBefore(SCHOOL_EXAM_DATE)) ExamType.SCHOOL_EXAM else ExamType.BOARD_EXAM
    }

    fun getSchoolExamTasks(): List<StudyTask> {
        return _tasks.value.filter { it.examPhase == ExamType.SCHOOL_EXAM }
    }

    fun getBoardExamTasks(): List<StudyTask> {
        return _tasks.value.filter { it.examPhase == ExamType.BOARD_EXAM }
    }

    fun getTasksBySubject(subjectId: String): List<StudyTask> {
        return _tasks.value.filter { it.subjectId == subjectId }
    }

    fun getSubjectTotalCount(subjectId: String): Int {
        return getTasksBySubject(subjectId).size
    }

    fun getSubjectCompletedCount(subjectId: String): Int {
        return getTasksBySubject(subjectId).count { it.isCompleted }
    }

    fun getSubjectProgress(subjectId: String): Float {
        val total = getSubjectTotalCount(subjectId)
        if (total == 0) return 0.0f
        return getSubjectCompletedCount(subjectId).toFloat() / total
    }

    fun getDailyProgress(): Float {
        val todayList = getTodayTasks()
        if (todayList.isEmpty()) return 0.0f
        val completed = todayList.count { it.isCompleted }
        return completed.toFloat() / todayList.size
    }

    fun getTodayCompletedCount(): Int = getTodayTasks().count { it.isCompleted }
    fun getTodayTotalCount(): Int = getTodayTasks().size

    fun getMissedCount(): Int = getMissedTasks().size

    fun getSchoolExamProgress(): Float {
        val schoolTasks = _tasks.value.filter { it.examPhase == ExamType.SCHOOL_EXAM }
        if (schoolTasks.isEmpty()) return 0.0f
        val completed = schoolTasks.count { it.isCompleted }
        return completed.toFloat() / schoolTasks.size
    }

    fun getOverallBoardProgress(): Float {
        if (_tasks.value.isEmpty()) return 0.0f
        val completed = _tasks.value.count { it.isCompleted }
        return completed.toFloat() / _tasks.value.size
    }

    // ==========================================
    // PROGRESS & STUDY STREAK (STEP 7)
    // ==========================================

    fun getCompletedCount(): Int = _tasks.value.count { it.isCompleted }

    fun getPendingCount(): Int = _tasks.value.count { !it.isCompleted }

    fun getTotalCount(): Int = _tasks.value.size

    /**
     * Daily completion percentage based solely on student-created To-Do tasks for TODAY.
     */
    fun getDailyCompletionPercentage(): Int {
        val todayTasks = getTodayTasks()
        if (todayTasks.isEmpty()) {
            val completedToday = _tasks.value.count { task ->
                task.isCompleted && task.completedAt != null && run {
                    try {
                        val compDate = java.time.Instant.ofEpochMilli(task.completedAt)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        compDate == TODAY
                    } catch (_: Exception) {
                        false
                    }
                }
            }
            return if (completedToday > 0) 100 else 0
        }
        val completedCount = todayTasks.count { it.isCompleted }
        return (completedCount * 100) / todayTasks.size
    }

    /**
     * Weekly completion percentage for the current calendar week (Monday to Sunday).
     */
    fun getWeeklyCompletionPercentage(): Int {
        val today = TODAY
        val startOfWeek = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val endOfWeek = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))

        val weekTasks = _tasks.value.filter { task ->
            task.scheduledDate?.let { !it.isBefore(startOfWeek) && !it.isAfter(endOfWeek) } ?: false
        }

        if (weekTasks.isEmpty()) {
            val completedThisWeek = _tasks.value.count { task ->
                task.isCompleted && task.completedAt != null && run {
                    try {
                        val compDate = java.time.Instant.ofEpochMilli(task.completedAt)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        !compDate.isBefore(startOfWeek) && !compDate.isAfter(endOfWeek)
                    } catch (_: Exception) {
                        false
                    }
                }
            }
            return if (completedThisWeek > 0) 100 else 0
        }

        val completedCount = weekTasks.count { it.isCompleted }
        return (completedCount * 100) / weekTasks.size
    }

    /**
     * Overall study progress percentage based on all student-created To-Do tasks.
     */
    fun getOverallCompletionPercentage(): Int {
        if (_tasks.value.isEmpty()) return 0
        val completed = _tasks.value.count { it.isCompleted }
        return (completed * 100) / _tasks.value.size
    }

    /**
     * Subject-wise completed tasks for all 7 official Class 12 Commerce subjects.
     * Calculated strictly from the student's own To-Do items.
     */
    fun getSubjectWiseProgressList(): List<SubjectProgressItem> {
        return Subject.OFFICIAL_SUBJECTS.map { sub ->
            val subTasks = _tasks.value.filter { it.subjectId == sub.id }
            val total = subTasks.size
            val completed = subTasks.count { it.isCompleted }
            val pending = total - completed
            val pct = if (total > 0) (completed * 100 / total) else 0
            SubjectProgressItem(
                subjectId = sub.id,
                subjectName = sub.name,
                completedCount = completed,
                pendingCount = pending,
                totalCount = total,
                progressPercentage = pct
            )
        }
    }

    /**
     * Study streak calculation based on completing at least one student-created To-Do on a day.
     * Requirements:
     * - Current streak
     * - Best streak
     * - Today's completion status ("Completed today" or "Pending today")
     * - Does NOT punish the student with automatic missed tasks.
     */
    fun getStudyStreak(): StudyStreak {
        val completedTasks = _tasks.value.filter { it.isCompleted }
        if (completedTasks.isEmpty()) {
            return StudyStreak(
                currentStreak = 0,
                bestStreak = 0,
                isCompletedToday = false,
                todayStatusText = "Pending today",
                streakDays = emptyList()
            )
        }

        val completedDates = mutableSetOf<LocalDate>()
        for (task in completedTasks) {
            if (task.completedAt != null) {
                try {
                    val date = java.time.Instant.ofEpochMilli(task.completedAt)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    completedDates.add(date)
                } catch (_: Exception) {}
            }
            task.scheduledDate?.let { completedDates.add(it) }
        }

        val today = TODAY
        val isCompletedToday = completedDates.contains(today)
        val todayStatusText = if (isCompletedToday) "Completed today" else "Pending today"

        // Current streak:
        // If completed today: streak counts today, today-1, today-2...
        // If not completed today: if yesterday was completed, streak is active/maintained!
        var currentStreak = 0
        var checkDate = if (isCompletedToday) today else today.minusDays(1)
        while (completedDates.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        // Best streak:
        val sortedDates = completedDates.sorted()
        var bestStreak = 0
        var runningStreak = 0
        var prevDate: LocalDate? = null

        for (d in sortedDates) {
            if (prevDate == null || d == prevDate.plusDays(1)) {
                runningStreak++
            } else if (d != prevDate) {
                runningStreak = 1
            }
            prevDate = d
            if (runningStreak > bestStreak) {
                bestStreak = runningStreak
            }
        }
        bestStreak = maxOf(bestStreak, currentStreak)

        return StudyStreak(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            isCompletedToday = isCompletedToday,
            todayStatusText = todayStatusText,
            streakDays = sortedDates
        )
    }

    /**
     * Full summary for the Progress section.
     */
    fun getProgressSummary(): StudyProgressSummary {
        val completed = getCompletedCount()
        val pending = getPendingCount()
        val missed = getMissedCount()
        val total = getTotalCount()
        val dailyPct = getDailyCompletionPercentage()
        val weeklyPct = getWeeklyCompletionPercentage()
        val overallPct = getOverallCompletionPercentage()
        val subjects = getSubjectWiseProgressList()
        val streak = getStudyStreak()

        return StudyProgressSummary(
            completedCount = completed,
            pendingCount = pending,
            missedCount = missed,
            totalCount = total,
            dailyPercentage = dailyPct,
            weeklyPercentage = weeklyPct,
            overallPercentage = overallPct,
            subjectProgressList = subjects,
            streak = streak
        )
    }

    /**
     * Get real remaining (uncompleted) chapters for a subject from SyllabusRepository.
     */
    fun getRemainingChaptersForSubject(subjectId: String): List<com.example.core.model.Chapter> {
        return SyllabusRepository.getPendingChapters(subjectId)
    }

    /**
     * Get real completed chapters for a subject from SyllabusRepository.
     */
    fun getCompletedChaptersForSubject(subjectId: String): List<com.example.core.model.Chapter> {
        return SyllabusRepository.getCompletedChapters(subjectId)
    }

    /**
     * Resets repository state for isolated testing.
     */
    fun resetForTesting() {
        _tasks.value = emptyList()
        currentDateOverride = null
    }
}
