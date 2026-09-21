package com.example.core.model

/**
 * Syllabus scope filter for viewing Full Board Exam syllabus vs School Exam (22 Oct 2026).
 */
enum class SyllabusScope(val displayName: String, val subtitle: String) {
    BOARD_EXAM(
        displayName = "Board Exam",
        subtitle = "Full Syllabus (100% Board Target)"
    ),
    SCHOOL_EXAM(
        displayName = "School Exam",
        subtitle = "22 October 2026 Milestone"
    )
}

/**
 * Chapter model representing an official Class 12 Gujarat Board textbook chapter.
 * Preserves the textbook's original language, terminology, and Part 1/Part 2 structure.
 * Stored in clean structured form ready for Step 5 chapter-wise To-Do task generation.
 */
data class Chapter(
    val id: String,
    val subjectId: String,
    val chapterNumber: Int,
    val title: String,
    val englishTitle: String? = null,
    val part: Int? = null,
    val partName: String? = null,
    val isGrammar: Boolean = false,
    val isInSchoolExam: Boolean = false,
    val points: Int = 10
)
