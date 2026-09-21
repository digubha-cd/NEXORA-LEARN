package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.model.TaskPriority
import com.example.core.reminder.DailyReminderConfig
import com.example.core.reminder.ReminderSlot
import com.example.core.reminder.StudyReminderManager
import com.example.core.reminder.StudyReminderReceiver
import com.example.core.repository.StudyPlannerRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime

/**
 * Unit and Robolectric tests verifying the Daily Planned Study Sessions Notification Scheduling System.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DailyStudyReminderTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        StudyPlannerRepository.resetForTesting()
    }

    @Test
    fun testDailyReminderConfigFormatting() {
        val morning = DailyReminderConfig(enabled = true, hour = 7, minute = 0)
        assertEquals("7:00 AM", morning.formattedTime)

        val evening = DailyReminderConfig(enabled = true, hour = 17, minute = 0)
        assertEquals("5:00 PM", evening.formattedTime)

        val night = DailyReminderConfig(enabled = true, hour = 21, minute = 30)
        assertEquals("9:30 PM", night.formattedTime)

        val midnight = DailyReminderConfig(enabled = true, hour = 0, minute = 15)
        assertEquals("12:15 AM", midnight.formattedTime)

        val noon = DailyReminderConfig(enabled = true, hour = 12, minute = 0)
        assertEquals("12:00 PM", noon.formattedTime)
    }

    @Test
    fun testPreferencesSaveAndRetrieve() {
        // Save Evening 5:00 PM
        StudyReminderManager.setDailyStudyReminderPreferences(
            context = context,
            enabled = true,
            hour = 17,
            minute = 0
        )

        val config = StudyReminderManager.getDailyStudyReminderPreferences(context)
        assertTrue("Daily reminder should be enabled", config.enabled)
        assertEquals(17, config.hour)
        assertEquals(0, config.minute)
        assertEquals("5:00 PM", config.formattedTime)

        // Disable reminder
        StudyReminderManager.setDailyStudyReminderPreferences(
            context = context,
            enabled = false,
            hour = 17,
            minute = 0
        )
        val disabledConfig = StudyReminderManager.getDailyStudyReminderPreferences(context)
        assertFalse("Daily reminder should be disabled", disabledConfig.enabled)
    }

    @Test
    fun testReminderSlotsMapping() {
        assertEquals(7, ReminderSlot.MORNING.hour)
        assertEquals(0, ReminderSlot.MORNING.minute)

        assertEquals(17, ReminderSlot.EVENING.hour)
        assertEquals(0, ReminderSlot.EVENING.minute)

        assertEquals(21, ReminderSlot.NIGHT.hour)
        assertEquals(0, ReminderSlot.NIGHT.minute)
    }

    @Test
    fun testEpochMillisComputation() {
        val testDate = LocalDate.of(2026, 10, 1)
        val testTime = LocalTime.of(7, 30)
        val epoch = StudyReminderManager.computeEpochMillis(testDate, testTime)
        assertTrue("Epoch millis must be positive", epoch > 0)
    }

    @Test
    fun testImmediateDailyStudySessionNotificationExecution() {
        // Add a planned task for today
        val today = StudyPlannerRepository.TODAY
        StudyPlannerRepository.addTask(
            title = "Accounts Part 1 Partnership Final Accounts",
            subjectId = "accounts",
            scheduledDate = today,
            priority = TaskPriority.HIGH,
            context = context
        )

        // Test sending immediate daily plan notification (should run without crashing)
        StudyReminderManager.sendImmediateDailyStudySessionNotification(context)

        val todayTasks = StudyPlannerRepository.getTodayTasks()
        assertEquals(1, todayTasks.size)
        assertEquals("Accounts Part 1 Partnership Final Accounts", todayTasks.first().title)
    }

    @Test
    fun testRescheduleAllPlannedStudyRemindersOnReboot() {
        // Add task with future reminder epoch
        val futureEpoch = System.currentTimeMillis() + 3600000L // 1 hour in future
        val task = StudyPlannerRepository.addTask(
            title = "Statistics Probability Chapter",
            subjectId = "stat",
            scheduledDate = StudyPlannerRepository.TODAY,
            reminderTime = "In 1 Hour",
            reminderEpochMillis = futureEpoch,
            context = context
        )

        assertNotNull(task.reminderEpochMillis)

        // Call batch reschedule
        StudyReminderManager.rescheduleAllPlannedStudyReminders(context)

        // Verify task still exists and holds scheduled epoch
        val tasks = StudyPlannerRepository.tasks.value
        assertTrue(tasks.any { it.taskId == task.taskId && it.reminderEpochMillis == futureEpoch })
    }

    @Test
    fun testCancelTaskReminder() {
        val task = StudyPlannerRepository.addTask(
            title = "Economics National Income",
            subjectId = "economics",
            scheduledDate = StudyPlannerRepository.TODAY,
            reminderTime = "Evening 5:00 PM",
            reminderEpochMillis = System.currentTimeMillis() + 7200000L,
            context = context
        )

        // Cancel reminder
        StudyReminderManager.cancelReminder(context, task.taskId)
        // Operation should complete smoothly without error
    }
}
