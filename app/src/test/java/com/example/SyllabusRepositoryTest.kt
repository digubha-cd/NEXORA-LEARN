package com.example

import com.example.core.model.SyllabusScope
import com.example.core.repository.SyllabusRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyllabusRepositoryTest {

    @Test
    fun `verify Economics official chapters and School Exam mapping`() {
        val allChapters = SyllabusRepository.getChapters("economics")
        assertEquals(11, allChapters.size)

        val schoolExamChapters = SyllabusRepository.getChaptersByScope("economics", SyllabusScope.SCHOOL_EXAM)
        assertEquals(7, schoolExamChapters.size)
        assertEquals("પ્રકરણ ૧: અર્થશાસ્ત્રમાં આલેખ", schoolExamChapters.first().title)
        assertEquals(7, schoolExamChapters.last().chapterNumber)
    }

    @Test
    fun `verify BA official chapters and School Exam mapping`() {
        val allChapters = SyllabusRepository.getChapters("ba")
        assertEquals(12, allChapters.size)

        val schoolExamChapters = SyllabusRepository.getChaptersByScope("ba", SyllabusScope.SCHOOL_EXAM)
        assertEquals(7, schoolExamChapters.size)
        assertEquals("પ્રકરણ ૧: સંચાલનનું સ્વરૂપ અને મહત્વ", schoolExamChapters.first().title)
        assertEquals(7, schoolExamChapters.last().chapterNumber)
    }

    @Test
    fun `verify English official units and School Exam mapping`() {
        val allUnits = SyllabusRepository.getChapters("english")
        assertEquals(11, allUnits.size) // 10 units + Grammar

        val schoolExamUnits = SyllabusRepository.getChaptersByScope("english", SyllabusScope.SCHOOL_EXAM)
        assertEquals(6, schoolExamUnits.size) // Units 1-5 + Grammar
        assertTrue(schoolExamUnits.any { it.isGrammar })
    }

    @Test
    fun `verify Gujarati official chapters and School Exam mapping`() {
        val allChapters = SyllabusRepository.getChapters("gujarati")
        assertEquals(25, allChapters.size) // 24 chapters + Grammar

        val schoolExamChapters = SyllabusRepository.getChaptersByScope("gujarati", SyllabusScope.SCHOOL_EXAM)
        assertEquals(21, schoolExamChapters.size) // Chapters 1-20 + Grammar
        assertTrue(schoolExamChapters.any { it.isGrammar })
        assertEquals("૧. અખિલ બ્રહ્માંડમાં (પદ)", schoolExamChapters.first().title)
    }

    @Test
    fun `verify Elements of Accounts Part 1 and Part 2 and School Exam mapping`() {
        val allChapters = SyllabusRepository.getChapters("accounts")
        assertEquals(13, allChapters.size) // 7 Part 1 + 6 Part 2

        val part1 = allChapters.filter { it.part == 1 }
        val part2 = allChapters.filter { it.part == 2 }
        assertEquals(7, part1.size)
        assertEquals(6, part2.size)

        val schoolExamChapters = SyllabusRepository.getChaptersByScope("accounts", SyllabusScope.SCHOOL_EXAM)
        assertEquals(7, schoolExamChapters.size) // Part 1 only
        assertTrue(schoolExamChapters.all { it.part == 1 })
    }

    @Test
    fun `verify Statistics Part 1 and Part 2 and School Exam mapping`() {
        val allChapters = SyllabusRepository.getChapters("stat")
        assertEquals(9, allChapters.size) // 4 Part 1 + 5 Part 2

        val part1 = allChapters.filter { it.part == 1 }
        val part2 = allChapters.filter { it.part == 2 }
        assertEquals(4, part1.size)
        assertEquals(5, part2.size)

        val schoolExamChapters = SyllabusRepository.getChaptersByScope("stat", SyllabusScope.SCHOOL_EXAM)
        assertEquals(5, schoolExamChapters.size) // Part 1 (4) + Part 2 Ch 1 (1)
        assertEquals(4, schoolExamChapters.count { it.part == 1 })
        assertEquals(1, schoolExamChapters.count { it.part == 2 })
    }

    @Test
    fun `verify SP and CC Part 1 and Part 2 and School Exam mapping`() {
        val allChapters = SyllabusRepository.getChapters("sp_cc")
        assertEquals(13, allChapters.size) // 6 Part 1 + 7 Part 2

        val part1 = allChapters.filter { it.part == 1 }
        val part2 = allChapters.filter { it.part == 2 }
        assertEquals(6, part1.size)
        assertEquals(7, part2.size)

        val schoolExamChapters = SyllabusRepository.getChaptersByScope("sp_cc", SyllabusScope.SCHOOL_EXAM)
        assertEquals(13, schoolExamChapters.size) // Part 1 + Part 2
    }

    @Test
    fun `verify chapter completion toggle`() {
        val chapterId = "eco_ch1"
        assertFalse(SyllabusRepository.isChapterCompleted(chapterId))

        SyllabusRepository.toggleChapterCompletion(chapterId)
        assertTrue(SyllabusRepository.isChapterCompleted(chapterId))

        SyllabusRepository.toggleChapterCompletion(chapterId)
        assertFalse(SyllabusRepository.isChapterCompleted(chapterId))
    }
}
