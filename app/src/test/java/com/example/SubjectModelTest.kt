package com.example

import com.example.core.model.Subject
import org.junit.Assert.assertEquals
import org.junit.Test

class SubjectModelTest {

    @Test
    fun `verify official 7 subjects for Gujarat Board Class 12 Commerce`() {
        val subjects = Subject.OFFICIAL_SUBJECTS
        assertEquals(7, subjects.size)

        val subjectNames = subjects.map { it.name }
        assertEquals("Gujarati", subjectNames[0])
        assertEquals("English", subjectNames[1])
        assertEquals("SP & CC", subjectNames[2])
        assertEquals("B.A.", subjectNames[3])
        assertEquals("Statistics", subjectNames[4])
        assertEquals("Elements of Accounts", subjectNames[5])
        assertEquals("Economics", subjectNames[6])
    }
}
