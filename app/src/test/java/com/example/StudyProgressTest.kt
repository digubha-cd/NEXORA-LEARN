package com.example

import com.example.core.model.Subject
import com.example.core.repository.SyllabusRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying live Study Progress, Chapter Completion states,
 * and Chapter Points calculation across all 7 Gujarat Board Class 12 Commerce subjects.
 */
class StudyProgressTest {

    @Before
    fun setup() {
        SyllabusRepository.resetAllCompletions()
    }

    @Test
    fun testAll7OfficialSubjectsExist() {
        val subjects = Subject.OFFICIAL_SUBJECTS
        assertEquals("Must have exactly 7 Class 12 Commerce subjects", 7, subjects.size)

        val subjectIds = subjects.map { it.id }.toSet()
        val expectedSubjectIds = setOf(
            "gujarati",
            "english",
            "sp_cc",
            "ba",
            "stat",
            "accounts",
            "economics"
        )
        assertEquals("Subject IDs must match official 7 subjects", expectedSubjectIds, subjectIds)

        // Verify each subject has non-empty chapters
        subjects.forEach { subject ->
            val chapters = SyllabusRepository.getChapters(subject.id)
            assertTrue("Subject ${subject.name} must contain chapters", chapters.isNotEmpty())
            assertTrue("Subject ${subject.name} must have maximum possible points > 0", SyllabusRepository.getSubjectMaxPoints(subject.id) > 0)
        }
    }

    @Test
    fun testInitialStudyProgressIsZero() {
        assertEquals(0, SyllabusRepository.getTotalCompletedChaptersCount())
        assertEquals(0, SyllabusRepository.getTotalEarnedPoints())
        assertEquals(0f, SyllabusRepository.getOverallProgress(), 0.001f)

        val totalChapters = SyllabusRepository.getTotalChaptersCount()
        assertTrue("Total chapters must be greater than 0", totalChapters > 0)
        assertEquals("All chapters should initially be pending", totalChapters, SyllabusRepository.getTotalPendingChaptersCount())

        Subject.OFFICIAL_SUBJECTS.forEach { subject ->
            assertEquals("Subject ${subject.name} completed count should be 0", 0, SyllabusRepository.getCompletedCount(subject.id, com.example.core.model.SyllabusScope.BOARD_EXAM))
            assertEquals("Subject ${subject.name} earned points should be 0", 0, SyllabusRepository.getSubjectEarnedPoints(subject.id))
            assertEquals("Subject ${subject.name} pending count should equal total chapters", SyllabusRepository.getChapters(subject.id).size, SyllabusRepository.getSubjectPendingCount(subject.id))
        }
    }

    @Test
    fun testToggleChapterCompletionEarnsAndRemovesPoints() {
        val accountsChapters = SyllabusRepository.getChapters("accounts")
        assertTrue("Accounts must have chapters", accountsChapters.isNotEmpty())
        val firstChapter = accountsChapters.first()

        // 1. Toggle completion ON
        SyllabusRepository.toggleChapterCompletion(firstChapter.id)
        assertTrue("First chapter should be completed", SyllabusRepository.isChapterCompleted(firstChapter.id))
        assertEquals(1, SyllabusRepository.getTotalCompletedChaptersCount())
        assertEquals(firstChapter.points, SyllabusRepository.getTotalEarnedPoints())
        assertEquals(firstChapter.points, SyllabusRepository.getSubjectEarnedPoints("accounts"))

        // 2. Toggle same chapter again (idempotent / no double-counting when toggled off)
        SyllabusRepository.toggleChapterCompletion(firstChapter.id)
        assertFalse("First chapter should be uncompleted", SyllabusRepository.isChapterCompleted(firstChapter.id))
        assertEquals(0, SyllabusRepository.getTotalCompletedChaptersCount())
        assertEquals(0, SyllabusRepository.getTotalEarnedPoints())
        assertEquals(0, SyllabusRepository.getSubjectEarnedPoints("accounts"))
    }

    @Test
    fun testMultipleSubjectCompletionsAndPointsAccumulation() {
        val gujaratiChapter = SyllabusRepository.getChapters("gujarati").first()
        val englishChapter = SyllabusRepository.getChapters("english").first()
        val accountsChapter = SyllabusRepository.getChapters("accounts").first()
        val statChapter = SyllabusRepository.getChapters("stat").first()
        val ecoChapter = SyllabusRepository.getChapters("economics").first()
        val baChapter = SyllabusRepository.getChapters("ba").first()
        val spccChapter = SyllabusRepository.getChapters("sp_cc").first()

        // Complete 1 chapter in each of the 7 subjects
        SyllabusRepository.setChapterCompleted(gujaratiChapter.id, true)
        SyllabusRepository.setChapterCompleted(englishChapter.id, true)
        SyllabusRepository.setChapterCompleted(accountsChapter.id, true)
        SyllabusRepository.setChapterCompleted(statChapter.id, true)
        SyllabusRepository.setChapterCompleted(ecoChapter.id, true)
        SyllabusRepository.setChapterCompleted(baChapter.id, true)
        SyllabusRepository.setChapterCompleted(spccChapter.id, true)

        assertEquals(7, SyllabusRepository.getTotalCompletedChaptersCount())

        val expectedPoints = gujaratiChapter.points +
                englishChapter.points +
                accountsChapter.points +
                statChapter.points +
                ecoChapter.points +
                baChapter.points +
                spccChapter.points

        assertEquals("Total points must equal sum of completed chapter points", expectedPoints, SyllabusRepository.getTotalEarnedPoints())

        // Verify setting completed again does not double-count points
        SyllabusRepository.setChapterCompleted(gujaratiChapter.id, true)
        assertEquals(expectedPoints, SyllabusRepository.getTotalEarnedPoints())

        // Uncheck one chapter
        SyllabusRepository.setChapterCompleted(gujaratiChapter.id, false)
        assertEquals(6, SyllabusRepository.getTotalCompletedChaptersCount())
        assertEquals(expectedPoints - gujaratiChapter.points, SyllabusRepository.getTotalEarnedPoints())
        assertEquals(0, SyllabusRepository.getSubjectEarnedPoints("gujarati"))
    }

    @Test
    fun testSubjectLevelProgressAndPendingCalculations() {
        val statChapters = SyllabusRepository.getChapters("stat")
        val statTotal = statChapters.size
        assertTrue("Statistics should have chapters", statTotal >= 4)

        // Complete 2 chapters in Statistics
        SyllabusRepository.setChapterCompleted(statChapters[0].id, true)
        SyllabusRepository.setChapterCompleted(statChapters[1].id, true)

        assertEquals(2, SyllabusRepository.getCompletedCount("stat", com.example.core.model.SyllabusScope.BOARD_EXAM))
        assertEquals(statTotal - 2, SyllabusRepository.getSubjectPendingCount("stat"))
        val expectedProgress = 2f / statTotal
        assertEquals(expectedProgress, SyllabusRepository.getProgress("stat", com.example.core.model.SyllabusScope.BOARD_EXAM), 0.001f)
        assertEquals(statChapters[0].points + statChapters[1].points, SyllabusRepository.getSubjectEarnedPoints("stat"))
    }
}
