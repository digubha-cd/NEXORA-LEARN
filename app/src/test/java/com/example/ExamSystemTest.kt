package com.example

import com.example.core.model.ExamPhase
import com.example.core.model.ExamType
import com.example.core.model.Subject
import com.example.core.model.SyllabusScope
import com.example.core.repository.ExamRepository
import com.example.core.repository.StudyPlannerRepository
import com.example.core.repository.SyllabusRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests verifying Step 6 Exam System:
 * - Two completely separate exam phases:
 *   1. School Exam (22 October 2026, Phase 1, 66 chapters)
 *   2. Board Exam (25 February 2027, Phase 2, 94 chapters across all 7 subjects)
 * - Dynamic Countdown Engine (never hard-coded)
 * - Phase Logic (School priority before 22 Oct, Board priority after 22 Oct)
 * - Separate progress calculations for School Exam, Board Exam, and 7 subjects
 * - Reactivity with Chapter checkboxes & Study Planner tasks
 * - Missed tasks detection and rescheduling
 */
class ExamSystemTest {

    @Before
    fun setUp() {
        StudyPlannerRepository.resetForTesting()
        ExamRepository.currentDate = LocalDate.of(2026, 9, 20)
        ExamRepository.setSelectedPhaseFilter(null)
    }

    @Test
    fun testTwoSeparateExamPhases() {
        assertEquals(
            "School Exam date must be 22 October 2026",
            LocalDate.of(2026, 10, 22),
            ExamRepository.SCHOOL_EXAM_DATE
        )
        assertEquals(
            "Board Exam date must be 25 February 2027",
            LocalDate.of(2027, 2, 25),
            ExamRepository.BOARD_EXAM_DATE
        )

        assertEquals("Phase 1 must be School Exam", ExamType.SCHOOL_EXAM, ExamPhase.PHASE_1_SCHOOL.type)
        assertEquals("Phase 2 must be Board Exam", ExamType.BOARD_EXAM, ExamPhase.PHASE_2_BOARD.type)
    }

    @Test
    fun testDynamicCountdownsFromGivenDate() {
        // On 20 September 2026:
        // School Exam (22 Oct 2026): 32 days remaining
        // Board Exam (25 Feb 2027): 158 days remaining
        val testDate = LocalDate.of(2026, 9, 20)

        val schoolDays = ExamRepository.getSchoolExamDaysRemaining(testDate)
        assertEquals("Days to School Exam on 20 Sep 2026 must be 32", 32L, schoolDays)

        val boardDays = ExamRepository.getBoardExamDaysRemaining(testDate)
        assertEquals("Days to Board Exam on 20 Sep 2026 must be 158", 158L, boardDays)

        val schoolCountdown = ExamRepository.formatSchoolExamCountdown(testDate)
        assertEquals("School Exam — 32 days", schoolCountdown)

        val boardCountdown = ExamRepository.formatBoardExamCountdown(testDate)
        assertEquals("Board Exam — 158 days", boardCountdown)

        // Dynamic check for another date: 1 October 2026
        val oct1 = LocalDate.of(2026, 10, 1)
        assertEquals(21L, ExamRepository.getSchoolExamDaysRemaining(oct1))
        assertEquals("School Exam — 21 days", ExamRepository.formatSchoolExamCountdown(oct1))
    }

    @Test
    fun testPhaseLogicBeforeAndAfterSchoolExam() {
        // Before 22 October 2026 -> Phase 1 Priority (School Exam)
        val beforeDate = LocalDate.of(2026, 9, 20)
        assertEquals(ExamPhase.PHASE_1_SCHOOL, ExamRepository.getActiveExamPhase(beforeDate))
        assertEquals(ExamType.SCHOOL_EXAM, StudyPlannerRepository.getActiveExamPhase(beforeDate))

        // On / After 22 October 2026 -> Phase 2 Priority (Board Exam)
        val onExamDate = LocalDate.of(2026, 10, 22)
        assertEquals(ExamPhase.PHASE_2_BOARD, ExamRepository.getActiveExamPhase(onExamDate))

        val afterDate = LocalDate.of(2026, 10, 25)
        assertEquals(ExamPhase.PHASE_2_BOARD, ExamRepository.getActiveExamPhase(afterDate))
        assertEquals(ExamType.BOARD_EXAM, StudyPlannerRepository.getActiveExamPhase(afterDate))
    }

    @Test
    fun testSchoolExamTasksDoNotMixBoardOnlyChapters() {
        // Add a student task for School Exam with a School Exam chapter
        val task = StudyPlannerRepository.addTask(
            title = "Accounts Partnership Final Accounts",
            subjectId = "accounts",
            chapterId = "acc_p1_ch2",
            scheduledDate = LocalDate.of(2026, 10, 15)
        )
        val schoolTasks = StudyPlannerRepository.getSchoolExamTasks()
        assertTrue("School Exam tasks must exist", schoolTasks.isNotEmpty())

        schoolTasks.forEach { st ->
            val chapter = SyllabusRepository.getChapterById(st.chapterId)
            assertNotNull("Chapter must exist for task", chapter)
            assertTrue(
                "School Exam task ${st.taskId} must belong to School Exam syllabus",
                chapter!!.isInSchoolExam
            )
        }
    }

    @Test
    fun testSyllabusScopeChapterCounts() {
        // School Exam syllabus (Phase 1): Exactly 66 chapters across all 7 subjects
        var totalSchoolChapters = 0
        var totalBoardChapters = 0

        StudyPlannerRepository.ORDERED_SUBJECT_IDS.forEach { subjectId ->
            val schoolList = SyllabusRepository.getChaptersByScope(subjectId, SyllabusScope.SCHOOL_EXAM)
            val allList = SyllabusRepository.getChapters(subjectId)

            totalSchoolChapters += schoolList.size
            totalBoardChapters += allList.size
        }

        assertEquals("Total School Exam chapters across 7 subjects must be 66", 66, totalSchoolChapters)
        assertEquals("Total Board Exam chapters across 7 subjects must be 94", 94, totalBoardChapters)
    }

    @Test
    fun testSeparateProgressCalculations() {
        val state = ExamRepository.dashboardState.value

        // Check initial state totals
        assertEquals(66, state.schoolExamTotalCount)
        assertEquals(94, state.boardExamTotalCount)
        assertEquals("90+ Marks", state.targetMarks)

        // All 7 subjects must be included in subject-wise progress
        assertEquals(7, state.subjectProgressList.size)
        val subjectIds = state.subjectProgressList.map { it.subjectId }.toSet()
        assertEquals(Subject.OFFICIAL_SUBJECTS.map { it.id }.toSet(), subjectIds)
    }

    @Test
    fun testProgressReactivityWithChapterCheckbox() {
        val initialSchoolCompleted = ExamRepository.dashboardState.value.schoolExamCompletedCount

        // Mark Accounts Ch 2 complete via ExamRepository (initially uncompleted)
        ExamRepository.toggleChapterCompletion("acc_p1_ch2")

        val updatedState = ExamRepository.dashboardState.value
        assertEquals(
            "School Exam completed chapters count should increment",
            initialSchoolCompleted + 1,
            updatedState.schoolExamCompletedCount
        )
        assertTrue(
            "School Exam progress percentage should be greater than 0",
            updatedState.schoolExamProgressPercent > 0.0f
        )

        // Uncheck chapter
        ExamRepository.toggleChapterCompletion("acc_p1_ch2")
        val revertedState = ExamRepository.dashboardState.value
        assertEquals(initialSchoolCompleted, revertedState.schoolExamCompletedCount)
    }

    @Test
    fun testMissedTasksAndRescheduling() {
        // Add a task in the past and mark it missed
        val missedTask = StudyPlannerRepository.addTask(
            title = "Past Task",
            subjectId = "accounts",
            scheduledDate = LocalDate.of(2026, 9, 15)
        )
        StudyPlannerRepository.setTaskMissed(missedTask.taskId, true)

        val initialMissed = StudyPlannerRepository.getMissedTasks()
        assertTrue("Baseline missed tasks exist for testing", initialMissed.isNotEmpty())

        val missedTaskId = initialMissed.first().taskId
        val initialMissedCount = initialMissed.size

        // Reschedule single task to today
        ExamRepository.rescheduleMissedTask(missedTaskId)

        val afterRescheduleMissed = StudyPlannerRepository.getMissedTasks()
        assertEquals(
            "Missed tasks count should decrement by 1",
            initialMissedCount - 1,
            afterRescheduleMissed.size
        )

        // Test reschedule all missed tasks
        val anotherMissed = StudyPlannerRepository.addTask(
            title = "Another Past Task",
            subjectId = "stat",
            scheduledDate = LocalDate.of(2026, 9, 10)
        )
        StudyPlannerRepository.setTaskMissed(anotherMissed.taskId, true)
        assertEquals(1, StudyPlannerRepository.getMissedTasks().size)

        ExamRepository.rescheduleAllMissedToToday()
        val finalMissed = StudyPlannerRepository.getMissedTasks()
        assertEquals("All missed tasks should now be cleared/rescheduled", 0, finalMissed.size)
    }
}
