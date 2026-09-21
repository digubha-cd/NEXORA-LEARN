package com.example.core.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.core.model.StudyTask
import com.example.core.model.Subject
import com.example.core.repository.StudyPlannerRepository
import com.example.core.repository.SyllabusRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Reminder time slots for student study sessions and remaining chapter notifications.
 */
enum class ReminderSlot(val label: String, val hour: Int, val minute: Int) {
    MORNING("Morning (7:00 AM)", 7, 0),
    EVENING("Evening (5:00 PM)", 17, 0),
    NIGHT("Night (9:00 PM)", 21, 0)
}

/**
 * Configuration data class for student's daily planned study reminder preferences.
 */
data class DailyReminderConfig(
    val enabled: Boolean = true,
    val hour: Int = 7,
    val minute: Int = 0
) {
    val formattedTime: String
        get() {
            val period = if (hour >= 12) "PM" else "AM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            val displayMinute = String.format("%02d", minute)
            return "$displayHour:$displayMinute $period"
        }
}

/**
 * Manager for scheduling and cancelling Android notifications for student-created To-Do tasks,
 * daily planned study sessions, and remaining chapter reminders.
 *
 * Requirements:
 * - Student chooses date/time or slot for a reminder.
 * - Send an Android notification at the selected time.
 * - Allow reminder edit/delete.
 * - Remind students of their planned study sessions for the day.
 * - Maintain exact / inexact AlarmManager lifecycle and boot persistence.
 * - Do NOT automatically create fake tasks or forced timetables.
 */
object StudyReminderManager {

    private const val TAG = "StudyReminderMgr"
    private const val PREFS_NAME = "nexora_study_reminders_pref"
    private const val KEY_DAILY_ENABLED = "daily_reminder_enabled"
    private const val KEY_DAILY_HOUR = "daily_reminder_hour"
    private const val KEY_DAILY_MINUTE = "daily_reminder_minute"

    private const val REQUEST_CODE_DAILY_PLAN = 88801

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Gets the saved daily study reminder configuration.
     */
    fun getDailyStudyReminderPreferences(context: Context): DailyReminderConfig {
        val prefs = getPrefs(context)
        val enabled = prefs.getBoolean(KEY_DAILY_ENABLED, true)
        val hour = prefs.getInt(KEY_DAILY_HOUR, 7) // Default 7:00 AM
        val minute = prefs.getInt(KEY_DAILY_MINUTE, 0)
        return DailyReminderConfig(enabled, hour, minute)
    }

    /**
     * Saves the daily study reminder preferences and schedules/cancels alarms accordingly.
     */
    fun setDailyStudyReminderPreferences(
        context: Context,
        enabled: Boolean,
        hour: Int = 7,
        minute: Int = 0
    ) {
        val prefs = getPrefs(context)
        prefs.edit()
            .putBoolean(KEY_DAILY_ENABLED, enabled)
            .putInt(KEY_DAILY_HOUR, hour)
            .putInt(KEY_DAILY_MINUTE, minute)
            .apply()

        if (enabled) {
            scheduleDailyStudySessionReminder(context, hour, minute)
        } else {
            cancelDailyStudySessionReminder(context)
        }
    }

    /**
     * Schedules the daily planned study sessions notification using AlarmManager.
     * Computes next occurrence (today if time is in future, or tomorrow if time has passed).
     */
    fun scheduleDailyStudySessionReminder(
        context: Context,
        hour: Int = 7,
        minute: Int = 0
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(hour, minute)
        var targetDateTime = LocalDateTime.of(now.toLocalDate(), targetTime)

        if (!targetDateTime.isAfter(now)) {
            targetDateTime = targetDateTime.plusDays(1)
        }

        val triggerEpochMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = StudyReminderReceiver.ACTION_DAILY_STUDY_SESSION_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_DAILY_PLAN,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled daily planned study session reminder at $targetDateTime ($triggerEpochMillis)")
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission restricted, falling back: ${e.message}")
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerEpochMillis, pendingIntent)
            } catch (ex: Exception) {
                Log.e(TAG, "Failed fallback daily alarm: ${ex.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule daily reminder: ${e.message}")
        }
    }

    /**
     * Cancels the daily study session reminder alarm.
     */
    fun cancelDailyStudySessionReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = StudyReminderReceiver.ACTION_DAILY_STUDY_SESSION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_DAILY_PLAN,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            try {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Cancelled daily planned study session reminder")
            } catch (e: Exception) {
                Log.w(TAG, "Error cancelling daily alarm: ${e.message}")
            }
        }
    }

    /**
     * Sends an immediate test/preview notification of today's planned study sessions.
     */
    fun sendImmediateDailyStudySessionNotification(context: Context) {
        val todayTasks = StudyPlannerRepository.getTodayTasks()
        val pendingTodayTasks = todayTasks.filter { !it.isCompleted }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            StudyReminderReceiver.NOTIFICATION_ID_DAILY_PLAN,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, contentText) = if (pendingTodayTasks.isNotEmpty()) {
            val count = pendingTodayTasks.size
            val subjectNames = pendingTodayTasks
                .map { it.subjectName }
                .filter { it.isNotBlank() && it != "General" }
                .distinct()

            val subjectsText = if (subjectNames.isNotEmpty()) {
                "in ${subjectNames.joinToString(", ")}"
            } else ""

            val titleStr = "Today's Study Plan • $count Planned Session${if (count > 1) "s" else ""}"
            val taskTitles = pendingTodayTasks.take(3).joinToString(" • ") { it.title }
            val moreSuffix = if (count > 3) " +${count - 3} more" else ""
            val bodyStr = "Planned for today $subjectsText: $taskTitles$moreSuffix. Stay on track for 90+ Marks!"

            Pair(titleStr, bodyStr)
        } else if (todayTasks.isNotEmpty() && pendingTodayTasks.isEmpty()) {
            Pair(
                "Great Job! Today's Study Plan Completed 🎉",
                "All planned study sessions for today are completed. Keep up your 90+ Marks momentum!"
            )
        } else {
            Pair(
                "Daily Study Reminder • Plan Today's Sessions",
                "You haven't scheduled any study sessions for today yet. Open Study Planner to organize today's chapters!"
            )
        }

        val notification = NotificationCompat.Builder(context, StudyReminderReceiver.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_study_reminder)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFF00E5FF.toInt())
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                StudyReminderReceiver.NOTIFICATION_ID_DAILY_PLAN,
                notification
            )
        } catch (_: Exception) {}
    }

    /**
     * Re-arms all future alarms across all planned tasks and daily reminders
     * (e.g. called after device reboot or app launch).
     */
    fun rescheduleAllPlannedStudyReminders(context: Context) {
        // 1. Reschedule daily planned study sessions summary if enabled
        val config = getDailyStudyReminderPreferences(context)
        if (config.enabled) {
            scheduleDailyStudySessionReminder(context, config.hour, config.minute)
        }

        // 2. Reschedule future individual task reminders
        val allTasks = StudyPlannerRepository.tasks.value
        val now = System.currentTimeMillis()
        allTasks.forEach { task ->
            val epoch = task.reminderEpochMillis
            if (epoch != null && epoch > now && !task.isCompleted) {
                scheduleReminder(context, task, epoch)
            }
        }
        Log.d(TAG, "Completed batch reschedule of planned study reminders on device reboot / startup")
    }

    /**
     * Schedules an Android AlarmManager notification for a student-created study task.
     */
    fun scheduleReminder(
        context: Context,
        task: StudyTask,
        reminderEpochMillis: Long
    ) {
        val now = System.currentTimeMillis()
        if (reminderEpochMillis <= now) {
            Log.d(TAG, "Reminder timestamp $reminderEpochMillis is in the past, skipping alarm")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = StudyReminderReceiver.ACTION_STUDY_REMINDER
            putExtra(StudyReminderReceiver.EXTRA_TASK_ID, task.taskId)
            putExtra(StudyReminderReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(StudyReminderReceiver.EXTRA_SUBJECT_NAME, task.subjectName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderEpochMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderEpochMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderEpochMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderEpochMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Successfully scheduled alarm for ${task.title} at $reminderEpochMillis")
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission restricted, falling back to standard alarm: ${e.message}")
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminderEpochMillis, pendingIntent)
            } catch (ex: Exception) {
                Log.e(TAG, "Failed fallback alarm: ${ex.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm: ${e.message}")
        }
    }

    /**
     * Cancels any scheduled alarm for the given task ID.
     */
    fun cancelReminder(context: Context, taskId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = StudyReminderReceiver.ACTION_STUDY_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            try {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Cancelled alarm for task $taskId")
            } catch (e: Exception) {
                Log.w(TAG, "Error cancelling alarm: ${e.message}")
            }
        }
    }

    /**
     * Schedules a reminder for remaining chapters of a given subject for a specific time slot
     * (Morning 7 AM, Evening 5 PM, Night 9 PM).
     */
    fun scheduleRemainingChaptersReminder(
        context: Context,
        subjectId: String,
        slot: ReminderSlot,
        date: LocalDate = LocalDate.now()
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val targetTime = LocalTime.of(slot.hour, slot.minute)
        var targetDateTime = LocalDateTime.of(date, targetTime)
        if (targetDateTime.isBefore(LocalDateTime.now())) {
            targetDateTime = targetDateTime.plusDays(1)
        }

        val reminderEpochMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = StudyReminderReceiver.ACTION_REMAINING_CHAPTERS_REMINDER
            putExtra(StudyReminderReceiver.EXTRA_SUBJECT_ID, subjectId)
            putExtra(StudyReminderReceiver.EXTRA_REMINDER_SLOT, slot.label)
        }

        val requestCode = ("remaining_${subjectId}_${slot.name}").hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderEpochMillis, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderEpochMillis, pendingIntent)
            }
            Log.d(TAG, "Scheduled remaining chapters reminder for $subjectId ($slot) at $targetDateTime")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling remaining chapters reminder: ${e.message}")
        }
    }

    /**
     * Cancels a scheduled remaining chapters reminder.
     */
    fun cancelRemainingChaptersReminder(
        context: Context,
        subjectId: String,
        slot: ReminderSlot
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = StudyReminderReceiver.ACTION_REMAINING_CHAPTERS_REMINDER
        }
        val requestCode = ("remaining_${subjectId}_${slot.name}").hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            try {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Cancelled remaining chapters reminder for $subjectId ($slot)")
            } catch (e: Exception) {
                Log.w(TAG, "Error cancelling remaining chapters reminder: ${e.message}")
            }
        }
    }

    /**
     * Sends an immediate remaining chapters notification (for verification / testing).
     */
    fun sendImmediateRemainingChaptersNotification(
        context: Context,
        subjectId: String,
        slot: ReminderSlot
    ) {
        val subject = Subject.OFFICIAL_SUBJECTS.firstOrNull { it.id == subjectId }
        val subjectName = subject?.name ?: (if (subjectId.isNotBlank()) subjectId else "All Subjects")
        val pendingChapters = SyllabusRepository.getPendingChapters(subjectId)

        val title = "$subjectName • ${slot.label} Reminder"
        val contentText = if (pendingChapters.isNotEmpty()) {
            val chaptersSummary = pendingChapters.joinToString(", ") { it.title }
            "Remaining chapters: $chaptersSummary. Complete these to earn chapter points towards 90+ Marks!"
        } else {
            "All chapters in $subjectName are completed! 🎉 Great work on keeping your 90+ Marks streak!"
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val notificationId = ("remaining_test_${subjectId}_${slot.name}").hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, StudyReminderReceiver.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_study_reminder)
            .setContentTitle(title)
            .setContentText(if (pendingChapters.isNotEmpty()) "${pendingChapters.size} remaining chapter(s) to study" else "All chapters completed!")
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFF00E5FF.toInt())
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: Exception) {}
    }

    /**
     * Formats notification title and text for remaining chapters preview.
     */
    fun formatRemainingChaptersPreview(
        subjectId: String,
        slot: ReminderSlot
    ): Pair<String, String> {
        val subject = Subject.OFFICIAL_SUBJECTS.firstOrNull { it.id == subjectId }
        val subjectName = subject?.name ?: (if (subjectId.isNotBlank()) subjectId else "All Subjects")
        val pendingChapters = SyllabusRepository.getPendingChapters(subjectId)

        val title = "$subjectName • ${slot.label}"
        val contentText = if (pendingChapters.isNotEmpty()) {
            val chaptersSummary = pendingChapters.joinToString(", ") { it.title }
            "Remaining chapters: $chaptersSummary. Target: 90+ Marks"
        } else {
            "All chapters in $subjectName are completed! 🎉"
        }
        return Pair(title, contentText)
    }

    /**
     * Helper to compute epoch millis from a date and time.
     */
    fun computeEpochMillis(date: LocalDate, time: LocalTime): Long {
        val localDateTime = LocalDateTime.of(date, time)
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
