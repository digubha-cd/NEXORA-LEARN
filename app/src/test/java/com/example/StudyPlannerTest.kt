package com.example

import com.example.core.model.Subject
import com.example.core.model.TaskPriority
import com.example.core.reminder.ReminderSlot
import com.example.core.reminder.StudyReminderManager
import com.example.core.repository.StudyPlannerRepository
import com.example.core.repository.SyllabusRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests verifying Student-Created To-Do and Automatic Exam Countdown behavior:
 * - NO automatic study-task generation (app does not force tasks)
 * - Student creates, edits, and deletes their own tasks
 * - Required / optional Subject selection with exactly 7 Class 12 Commerce subjects
 * - Chapter selection from real SyllabusRepository
 * - Checkbox marks tasks complete and syncs chapter points, subject progress, and profile progress
 * - Filtering into Pending and Completed
 * - Optional date assignment
 * - Remaining chapters reminders for Morning, Evening, Night
 * - Automatic exam countdown calculations (32 -> 31 -> 30, EXAM DAY, never negative)
 */
class StudyPlannerTest {

    @Before
    fun setUp() {
        StudyPlannerRepository.resetForTesting()
        SyllabusRepository.resetAllCompletions()
    }

    @Test
    fun testAllSevenClass12CommerceSubjectsAvailable() {
        val expectedSubjectNames = listOf(
            "Gujarati",
            "English",
            "SP & CC",
            "B.A.",
            "Statistics",
            "Elements of Accounts",
            "Economics"
        )
        val officialNames = Subject.OFFICIAL_SUBJECTS.map { it.name }
        assertEquals(7, Subject.OFFICIAL_SUBJECTS.size)
        expectedSubjectNames.forEach { expected ->
            assertTrue("Expected subject $expected in official subjects", officialNames.contains(expected))
        }
    }

    @Test
    fun testNoAutomaticTaskGenerationOnInitialization() {
        val tasks = StudyPlannerRepository.tasks.value
        assertTrue("Automatic study-task generation must be completely removed", tasks.isEmpty())
    }

    @Test
    fun testStudentCanAddEditAndDeleteTask() {
        // 1. Student adds a task
        val task = StudyPlannerRepository.addTask(
            title = "Revise Accounts Part 1 Partnership final accounts",
            description = "Solve illustration 1 and 2",
            subjectId = "accounts",
            scheduledDate = LocalDate.of(2026, 9, 21),
            priority = TaskPriority.HIGH
        )
        assertNotNull(task)
        assertEquals(1, StudyPlannerRepository.tasks.value.size)
        assertEquals("Revise Accounts Part 1 Partnership final accounts", task.title)
        assertEquals("Elements of Accounts", task.subjectName)
        assertEquals(TaskPriority.HIGH, task.priority)
        assertFalse(task.isCompleted)

        // 2. Student edits the task
        StudyPlannerRepository.updateTask(
            taskId = task.taskId,
            title = "Revise Accounts Part 1 (Updated)",
            description = "Solve illustration 1, 2, and 3",
            subjectId = "accounts",
            scheduledDate = LocalDate.of(2026, 9, 22),
            priority = TaskPriority.MEDIUM
        )
        val updated = StudyPlannerRepository.tasks.value.first()
        assertEquals("Revise Accounts Part 1 (Updated)", updated.title)
        assertEquals("Solve illustration 1, 2, and 3", updated.description)
        assertEquals("2026-09-22", updated.scheduledDateStr)
        assertEquals(TaskPriority.MEDIUM, updated.priority)

        // 3. Student deletes the task
        StudyPlannerRepository.deleteTask(task.taskId)
        assertTrue("Task should be deleted", StudyPlannerRepository.tasks.value.isEmpty())
    }

    @Test
    fun testStudentCanMarkCompleteWithCheckbox() {
        val task = StudyPlannerRepository.addTask(
            title = "Economics Chapter 1 Revision",
            description = "Review graph questions",
            subjectId = "economics",
            scheduledDate = null,
            priority = TaskPriority.MEDIUM
        )

        assertFalse(task.isCompleted)

        // Toggle to complete
        StudyPlannerRepository.toggleTaskCompletion(task.taskId)
        val completedTask = StudyPlannerRepository.tasks.value.first()
        assertTrue(completedTask.isCompleted)

        // Toggle back to incomplete
        StudyPlannerRepository.toggleTaskCompletion(task.taskId)
        val revertedTask = StudyPlannerRepository.tasks.value.first()
        assertFalse(revertedTask.isCompleted)
    }

    @Test
    fun testChapterLinkedTodoUpdatesSyllabusProgressAndPoints() {
        // Initial state: 0 completed chapters, 0 points
        assertEquals(0, SyllabusRepository.getTotalCompletedChaptersCount())
        assertEquals(0, SyllabusRepository.getTotalEarnedPoints())

        val accountsChapters = SyllabusRepository.getChapters("accounts")
        assertTrue("Accounts must have real chapters", accountsChapters.isNotEmpty())
        val firstChapter = accountsChapters.first()
        val secondChapter = accountsChapters[1]

        // Add a task linked to two actual chapters
        val task = StudyPlannerRepository.addTask(
            title = "Accounts Partnership Chapters",
            subjectId = "accounts",
            chapterIds = listOf(firstChapter.id, secondChapter.id)
        )

        // Verify task has the chapterIds
        assertEquals(listOf(firstChapter.id, secondChapter.id), task.chapterIds)
        assertFalse(SyllabusRepository.isChapterCompleted(firstChapter.id))
        assertFalse(SyllabusRepository.isChapterCompleted(secondChapter.id))

        // Student ticks / completes the task
        StudyPlannerRepository.toggleTaskCompletion(task.taskId)

        // Chapters should now be completed in SyllabusRepository
        assertTrue(SyllabusRepository.isChapterCompleted(firstChapter.id))
        assertTrue(SyllabusRepository.isChapterCompleted(secondChapter.id))
        assertEquals(2, SyllabusRepository.getTotalCompletedChaptersCount())

        val expectedPoints = firstChapter.points + secondChapter.points
        assertEquals(expectedPoints, SyllabusRepository.getTotalEarnedPoints())
        assertEquals(expectedPoints, SyllabusRepository.getSubjectEarnedPoints("accounts"))
        assertEquals(2, SyllabusRepository.getCompletedChapters("accounts").size)

        // Student un-ticks / reverts the task
        StudyPlannerRepository.toggleTaskCompletion(task.taskId)
        assertFalse(SyllabusRepository.isChapterCompleted(firstChapter.id))
        assertFalse(SyllabusRepository.isChapterCompleted(secondChapter.id))
        assertEquals(0, SyllabusRepository.getTotalCompletedChaptersCount())
        assertEquals(0, SyllabusRepository.getTotalEarnedPoints())
    }

    @Test
    fun testRemainingChaptersReminderPreview() {
        // Initially all chapters in Statistics are pending
        val statChapters = SyllabusRepository.getChapters("stat")
        val statPending = SyllabusRepository.getPendingChapters("stat")
        assertEquals(statChapters.size, statPending.size)

        val (morningTitle, morningText) = StudyReminderManager.formatRemainingChaptersPreview("stat", ReminderSlot.MORNING)
        assertTrue(morningTitle.contains("Statistics"))
        assertTrue(morningTitle.contains("Morning"))
        assertTrue(morningText.contains("Remaining chapters:"))
        assertTrue(morningText.contains(statChapters.first().title))

        val (eveningTitle, eveningText) = StudyReminderManager.formatRemainingChaptersPreview("stat", ReminderSlot.EVENING)
        assertTrue(eveningTitle.contains("Evening"))

        val (nightTitle, nightText) = StudyReminderManager.formatRemainingChaptersPreview("stat", ReminderSlot.NIGHT)
        assertTrue(nightTitle.contains("Night"))
    }

    @Test
    fun testPendingAndCompletedFiltering() {
        val task1 = StudyPlannerRepository.addTask(
            title = "Study Statistics Probability",
            subjectId = "stat",
            scheduledDate = LocalDate.of(2026, 9, 21)
        )
        val task2 = StudyPlannerRepository.addTask(
            title = "Read English Unit 1",
            subjectId = "english",
            scheduledDate = null
        )

        // Initially both are pending
        assertEquals(2, StudyPlannerRepository.getPendingTasks().size)
        assertEquals(0, StudyPlannerRepository.getCompletedTasks().size)

        // Mark task1 complete
        StudyPlannerRepository.toggleTaskCompletion(task1.taskId)

        val pending = StudyPlannerRepository.getPendingTasks()
        val completed = StudyPlannerRepository.getCompletedTasks()

        assertEquals(1, pending.size)
        assertEquals(task2.taskId, pending.first().taskId)

        assertEquals(1, completed.size)
        assertEquals(task1.taskId, completed.first().taskId)
    }

    @Test
    fun testOptionalDateAssignment() {
        val taskWithDate = StudyPlannerRepository.addTask(
            title = "Task with Date",
            scheduledDate = LocalDate.of(2026, 10, 5)
        )
        val taskWithoutDate = StudyPlannerRepository.addTask(
            title = "Task without Date",
            scheduledDate = null
        )

        assertTrue(taskWithDate.hasDate)
        assertEquals("2026-10-05", taskWithDate.scheduledDateStr)

        assertFalse(taskWithoutDate.hasDate)
        assertNull(taskWithoutDate.scheduledDate)
        assertEquals("", taskWithoutDate.scheduledDateStr)
        assertEquals("No date assigned", taskWithoutDate.scheduledDateFormatted)
    }

    @Test
    fun testAutomaticExamCountdownDecreasesDaily() {
        val sep20 = LocalDate.of(2026, 9, 20)
        val sep21 = LocalDate.of(2026, 9, 21)
        val sep22 = LocalDate.of(2026, 9, 22)

        val days20 = StudyPlannerRepository.getSchoolExamDaysRemaining(sep20)
        val days21 = StudyPlannerRepository.getSchoolExamDaysRemaining(sep21)
        val days22 = StudyPlannerRepository.getSchoolExamDaysRemaining(sep22)

        assertEquals(32L, days20)
        assertEquals(31L, days21)
        assertEquals(30L, days22)

        assertEquals("32 days", StudyPlannerRepository.formatDays(days20))
        assertEquals("31 days", StudyPlannerRepository.formatDays(days21))
        assertEquals("30 days", StudyPlannerRepository.formatDays(days22))
    }

    @Test
    fun testExamDayDisplaysExamDayAndNeverNegative() {
        // On School Exam Day (22 October 2026)
        val schoolExamDate = LocalDate.of(2026, 10, 22)
        val schoolDaysOnDate = StudyPlannerRepository.getSchoolExamDaysRemaining(schoolExamDate)
        assertEquals(0L, schoolDaysOnDate)
        assertEquals("EXAM DAY", StudyPlannerRepository.formatDays(schoolDaysOnDate))

        // After School Exam Date (Never negative)
        val dayAfterSchool = LocalDate.of(2026, 10, 23)
        val schoolDaysAfter = StudyPlannerRepository.getSchoolExamDaysRemaining(dayAfterSchool)
        assertEquals(0L, schoolDaysAfter)
        assertEquals("EXAM DAY", StudyPlannerRepository.formatDays(schoolDaysAfter))

        // On Board Exam Day (25 February 2027)
        val boardExamDate = LocalDate.of(2027, 2, 25)
        val boardDaysOnDate = StudyPlannerRepository.getBoardExamDaysRemaining(boardExamDate)
        assertEquals(0L, boardDaysOnDate)
        assertEquals("EXAM DAY", StudyPlannerRepository.formatDays(boardDaysOnDate))

        // After Board Exam Date (Never negative)
        val dayAfterBoard = LocalDate.of(2027, 3, 1)
        val boardDaysAfter = StudyPlannerRepository.getBoardExamDaysRemaining(dayAfterBoard)
        assertEquals(0L, boardDaysAfter)
        assertEquals("EXAM DAY", StudyPlannerRepository.formatDays(boardDaysAfter))
    }
}
