package com.example.core.model

import java.time.LocalDate

enum class ExamType {
    SCHOOL_EXAM,
    BOARD_EXAM
}

/**
 * Represents the two completely separate exam phases for Class 12 Commerce:
 * Phase 1: School Exam (22 October 2026) - Step 4 School Exam syllabus only.
 * Phase 2: Board Exam (25 February 2027) - Complete syllabus of all 7 subjects.
 */
enum class ExamPhase(
    val type: ExamType,
    val phaseNumber: Int,
    val title: String,
    val examName: String,
    val examDateStr: String,
    val syllabusDescription: String
) {
    PHASE_1_SCHOOL(
        type = ExamType.SCHOOL_EXAM,
        phaseNumber = 1,
        title = "Phase 1: School Exam",
        examName = "School Exam",
        examDateStr = "22 October 2026",
        syllabusDescription = "School Exam Syllabus (66 Chapters)"
    ),
    PHASE_2_BOARD(
        type = ExamType.BOARD_EXAM,
        phaseNumber = 2,
        title = "Phase 2: Board Exam",
        examName = "Board Exam",
        examDateStr = "25 February 2027",
        syllabusDescription = "Complete Syllabus (All 7 Subjects • 94 Chapters)"
    )
}

/**
 * Subject-wise preparation progress model across both School and Board exam milestones.
 */
data class SubjectExamProgress(
    val subjectId: String,
    val subjectName: String,
    val gujaratiName: String,
    val schoolExamCompleted: Int,
    val schoolExamTotal: Int,
    val schoolExamPercent: Float,
    val boardExamCompleted: Int,
    val boardExamTotal: Int,
    val boardExamPercent: Float
)

/**
 * Complete immutable snapshot of the Exam Dashboard state.
 */
data class ExamDashboardState(
    val targetMarks: String = "90+ Marks",
    val activePhase: ExamPhase = ExamPhase.PHASE_1_SCHOOL,
    val isSchoolExamPriority: Boolean = true,
    // Dynamic countdowns
    val schoolExamDaysRemaining: Long = 32L,
    val schoolExamCountdownText: String = "School Exam — 32 days remaining",
    val boardExamDaysRemaining: Long = 158L,
    val boardExamCountdownText: String = "Board Exam — 158 days remaining",
    // School Exam progress (Phase 1)
    val schoolExamCompletedCount: Int = 0,
    val schoolExamTotalCount: Int = 66,
    val schoolExamProgressPercent: Float = 0.0f,
    // Board Exam progress (Phase 2)
    val boardExamCompletedCount: Int = 0,
    val boardExamTotalCount: Int = 94,
    val boardExamProgressPercent: Float = 0.0f,
    // Overall preparation percentage
    val overallProgressPercent: Float = 0.0f,
    // Subject-wise progress (7 subjects)
    val subjectProgressList: List<SubjectExamProgress> = emptyList(),
    // Chapter counts
    val completedChaptersCount: Int = 0,
    val pendingChaptersCount: Int = 66,
    // Missed tasks
    val missedTasksCount: Int = 0,
    val missedTasks: List<StudyTask> = emptyList()
)

/**
 * Exam milestone model supporting progress tracking towards the 90+ target.
 */
data class Exam(
    val examId: String,
    val name: String,
    val date: String,
    val type: ExamType,
    val target: String = "90+ Marks",
    val progress: Float = 0.0f
)

