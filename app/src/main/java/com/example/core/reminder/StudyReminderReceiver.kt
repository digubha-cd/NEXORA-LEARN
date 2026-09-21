package com.example.core.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.core.model.Subject
import com.example.core.repository.StudyPlannerRepository
import com.example.core.repository.SyllabusRepository

/**
 * BroadcastReceiver triggered by AlarmManager when a student's study reminder is due.
 * Sends an Android notification with high priority, task title, subject, and direct intent to open NEXORA LEARN.
 *
 * Supports:
 * 1. Individual study session reminders for planned tasks
 * 2. Daily planned study sessions summary notifications (Morning / Evening / Night / Custom)
 * 3. Remaining chapters reminders with real chapter titles from SyllabusRepository
 * 4. Inline task completion action from notification
 * 5. Device reboot alarm restoration
 */
class StudyReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "nexora_study_reminders"
        const val CHANNEL_NAME = "NEXORA Study Reminders"
        const val ACTION_STUDY_REMINDER = "com.example.ACTION_STUDY_REMINDER"
        const val ACTION_DAILY_STUDY_SESSION_REMINDER = "com.example.ACTION_DAILY_STUDY_SESSION_REMINDER"
        const val ACTION_REMAINING_CHAPTERS_REMINDER = "com.example.ACTION_REMAINING_CHAPTERS_REMINDER"
        const val ACTION_COMPLETE_TASK_FROM_NOTIFICATION = "com.example.ACTION_COMPLETE_TASK_FROM_NOTIFICATION"

        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_SUBJECT_ID = "extra_subject_id"
        const val EXTRA_SUBJECT_NAME = "extra_subject_name"
        const val EXTRA_REMINDER_SLOT = "extra_reminder_slot"

        const val NOTIFICATION_ID_DAILY_PLAN = 99901
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("StudyReminderReceiver", "Received broadcast action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            StudyReminderManager.rescheduleAllPlannedStudyReminders(context)
            return
        }

        if (action == ACTION_COMPLETE_TASK_FROM_NOTIFICATION) {
            val taskId = intent.getStringExtra(EXTRA_TASK_ID)
            if (!taskId.isNullOrBlank()) {
                StudyPlannerRepository.toggleTaskCompletion(taskId)
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.cancel(taskId.hashCode())
            }
            return
        }

        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        when (action) {
            ACTION_DAILY_STUDY_SESSION_REMINDER -> {
                handleDailyStudySessionsReminder(context, openAppIntent)
                // Schedule next occurrence for tomorrow
                val config = StudyReminderManager.getDailyStudyReminderPreferences(context)
                if (config.enabled) {
                    StudyReminderManager.scheduleDailyStudySessionReminder(context, config.hour, config.minute)
                }
            }
            ACTION_REMAINING_CHAPTERS_REMINDER -> {
                handleRemainingChaptersReminder(context, intent, openAppIntent)
            }
            else -> {
                handleTaskReminder(context, intent, openAppIntent)
            }
        }
    }

    private fun handleDailyStudySessionsReminder(context: Context, openAppIntent: Intent) {
        val todayTasks = StudyPlannerRepository.getTodayTasks()
        val pendingTodayTasks = todayTasks.filter { !it.isCompleted }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_DAILY_PLAN,
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

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
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
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DAILY_PLAN, notification)
        } catch (_: SecurityException) {
        } catch (_: Exception) {
        }
    }

    private fun handleTaskReminder(context: Context, intent: Intent, openAppIntent: Intent) {
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Study Session"
        val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: ""

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Mark Complete directly from notification
        val completeIntent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = ACTION_COMPLETE_TASK_FROM_NOTIFICATION
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId + "_complete").hashCode(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (subjectName.isNotBlank() && subjectName != "General") {
            "$subjectName • Time for your planned study session! Target: 90+ Marks"
        } else {
            "Time for your planned study session! Target: 90+ Marks"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_study_reminder)
            .setContentTitle("Study Reminder: $taskTitle")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "Mark Done",
                completePendingIntent
            )
            .setColor(0xFF00E5FF.toInt()) // NexoraCyan
            .build()

        try {
            NotificationManagerCompat.from(context).notify(taskId.hashCode(), notification)
        } catch (_: SecurityException) {
        } catch (_: Exception) {
        }
    }

    private fun handleRemainingChaptersReminder(context: Context, intent: Intent, openAppIntent: Intent) {
        val subjectId = intent.getStringExtra(EXTRA_SUBJECT_ID) ?: ""
        val slotName = intent.getStringExtra(EXTRA_REMINDER_SLOT) ?: "Study"
        val subject = Subject.OFFICIAL_SUBJECTS.firstOrNull { it.id == subjectId }
        val subjectName = subject?.name ?: (if (subjectId.isNotBlank()) subjectId else "All Subjects")

        val pendingChapters = if (subjectId.isNotBlank()) {
            SyllabusRepository.getPendingChapters(subjectId)
        } else {
            emptyList()
        }

        val notificationId = ("remaining_${subjectId}_$slotName").hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "$subjectName • $slotName Reminder"
        val contentText = if (pendingChapters.isNotEmpty()) {
            val chaptersSummary = pendingChapters.joinToString(", ") { it.title }
            "Remaining chapters: $chaptersSummary. Complete these to earn chapter points towards 90+ Marks!"
        } else {
            "All chapters in $subjectName are completed! 🎉 Great work on keeping your 90+ Marks streak!"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
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
        } catch (_: SecurityException) {
        } catch (_: Exception) {
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for student planned study sessions in NEXORA LEARN"
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
