package com.example.core.model

enum class TaskPriority(val label: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

enum class TaskStatus(val label: String) {
    PENDING("Pending"),
    COMPLETED("Completed"),
    MISSED("Missed")
}

enum class PlannerFilterTab(val label: String) {
    PENDING("Pending"),
    COMPLETED("Completed"),
    PROGRESS("Progress & Streak"),
    CALENDAR("Calendar"),
    ALL("All Tasks"),
    TODAY("Today"),
    UPCOMING("Upcoming"),
    MISSED("Missed"),
    SUBJECTS_TODO("Subject To-Do")
}

/**
 * Study Task model for student-created To-Do items.
 * Allows students to add, edit, delete, mark complete, and optionally assign dates/subjects.
 */
data class StudyTask(
    val taskId: String,
    val title: String,
    val description: String = "",
    val subjectId: String = "", // e.g. "accounts", "stat", "economics", "ba", "sp_cc", "english", "gujarati", or ""
    val subjectName: String = "",
    val chapterId: String = "",
    val chapterTitle: String = "",
    val chapterIds: List<String> = emptyList(),
    val examPhase: ExamType = ExamType.SCHOOL_EXAM,
    val scheduledDateStr: String = "", // ISO date "YYYY-MM-DD" or empty if no date assigned
    val scheduledDateFormatted: String = "", // e.g. "21 Sep 2026" or "No date"
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val isMissed: Boolean = false,
    val rescheduled: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val durationMinutes: Int = 30,
    val notes: String = "",
    val reminderTime: String? = null,
    val reminderDateStr: String? = null,
    val reminderEpochMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val status: TaskStatus
        get() = when {
            isCompleted -> TaskStatus.COMPLETED
            isMissed -> TaskStatus.MISSED
            else -> TaskStatus.PENDING
        }

    val hasDate: Boolean
        get() = scheduledDateStr.isNotBlank()

    val scheduledDate: java.time.LocalDate?
        get() = try {
            if (scheduledDateStr.isNotBlank()) java.time.LocalDate.parse(scheduledDateStr) else null
        } catch (_: Exception) {
            null
        }

    val hasReminder: Boolean
        get() = reminderEpochMillis != null || !reminderTime.isNullOrBlank()

    val reminderFormatted: String
        get() {
            if (reminderEpochMillis != null) {
                return try {
                    val dateTime = java.time.Instant.ofEpochMilli(reminderEpochMillis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime()
                    dateTime.format(java.time.format.DateTimeFormatter.ofPattern("d MMM, h:mm a", java.util.Locale.ENGLISH))
                } catch (e: Exception) {
                    reminderTime ?: ""
                }
            }
            if (!reminderTime.isNullOrBlank()) {
                return if (!reminderDateStr.isNullOrBlank()) "$reminderDateStr, $reminderTime" else reminderTime
            }
            return ""
        }

    val examPhaseBadge: String
        get() = when (examPhase) {
            ExamType.SCHOOL_EXAM -> "School Exam (22 Oct 2026)"
            ExamType.BOARD_EXAM -> "Board Exam (25 Feb 2027)"
        }

    val durationFormatted: String
        get() = "${durationMinutes} mins"
}

/**
 * Model representing a date's student tasks and exam countdown status.
 */
data class DailyStudyPlan(
    val dateStr: String,
    val dateFormatted: String,
    val totalHours: Int = 0,
    val totalMinutes: Int = 0,
    val tasks: List<StudyTask> = emptyList(),
    val schoolExamDaysRemaining: Long = 0,
    val boardExamDaysRemaining: Long = 0
)

