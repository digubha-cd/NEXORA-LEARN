package com.example.core.repository

import com.example.core.constants.AppConstants
import com.example.core.model.Chapter
import com.example.core.model.ExamDashboardState
import com.example.core.model.ExamPhase
import com.example.core.model.ExamType
import com.example.core.model.StudyTask
import com.example.core.model.Subject
import com.example.core.model.SubjectExamProgress
import com.example.core.model.SyllabusScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Repository powering Step 6 Exam System:
 * 1. Two separate exam phases:
 *    - School Exam (22 October 2026): Phase 1 Priority (66 chapters from Step 4 School Exam syllabus).
 *    - Board Exam (25 February 2027): Phase 2 Priority (Complete 94 chapters across all 7 subjects).
 * 2. Dynamic Exam Countdowns:
 *    - "School Exam — X days remaining"
 *    - "Board Exam — Y days remaining"
 *    Calculated automatically from the current reference date.
 * 3. Phase Logic:
 *    - Before 22 October 2026: Prioritize School Exam. Never mix Board Exam exclusive tasks into School Exam.
 *    - After School Exam: Automatically switch priority to Board Exam with the complete Board syllabus.
 * 4. Separate Progress Calculations:
 *    - School Exam progress (% and chapters)
 *    - Board Exam progress (% and chapters)
 *    - Overall 7-subject progress
 *    - Subject-wise preparation breakdown
 * 5. Full two-way reactivity with Step 4 Chapter Checkboxes & Step 5 Study Planner tasks.
 */
object ExamRepository {

    // Anchor reference date (dynamic support)
    var currentDate: LocalDate = StudyPlannerRepository.TODAY

    val SCHOOL_EXAM_DATE: LocalDate get() = StudyPlannerRepository.SCHOOL_EXAM_DATE
    val BOARD_EXAM_DATE: LocalDate get() = StudyPlannerRepository.BOARD_EXAM_DATE

    private val repositoryScope = CoroutineScope(Dispatchers.Default)

    // Manual view-phase override if student wants to inspect Phase 1 vs Phase 2 in UI
    private val _selectedPhaseFilter = MutableStateFlow<ExamPhase?>(null)
    val selectedPhaseFilter: StateFlow<ExamPhase?> = _selectedPhaseFilter

    private val _dashboardState = MutableStateFlow(calculateCurrentState())
    val dashboardState: StateFlow<ExamDashboardState> = _dashboardState

    init {
        kotlinx.coroutines.CoroutineScope(Dispatchers.Default).launch {
            combine(
                SyllabusRepository.completedChapterIds,
                StudyPlannerRepository.tasks,
                _selectedPhaseFilter
            ) { completedIds, allTasks, selectedPhase ->
                calculateDashboardState(completedIds, allTasks, selectedPhase)
            }.collect { newState ->
                _dashboardState.value = newState
            }
        }
    }

    fun setSelectedPhaseFilter(phase: ExamPhase?) {
        _selectedPhaseFilter.value = phase
        refreshDashboardState()
    }

    /**
     * Synchronously computes the current dashboard state snapshot.
     */
    fun calculateCurrentState(): ExamDashboardState {
        return calculateDashboardState(
            SyllabusRepository.completedChapterIds.value,
            StudyPlannerRepository.tasks.value,
            _selectedPhaseFilter.value
        )
    }

    /**
     * Refreshes the active dashboard state immediately.
     */
    fun refreshDashboardState(): ExamDashboardState {
        val newState = calculateCurrentState()
        _dashboardState.value = newState
        return newState
    }

    fun resetForTesting() {
        currentDate = StudyPlannerRepository.TODAY
        _selectedPhaseFilter.value = null
        refreshDashboardState()
    }

    /**
     * Determines the active exam phase based on current date.
     * Before 22 October 2026: Phase 1 (School Exam).
     * On/After 22 October 2026: Phase 2 (Board Exam).
     */
    fun getActiveExamPhase(date: LocalDate = currentDate): ExamPhase {
        return if (date.isBefore(SCHOOL_EXAM_DATE)) {
            ExamPhase.PHASE_1_SCHOOL
        } else {
            ExamPhase.PHASE_2_BOARD
        }
    }

    /**
     * Dynamically calculates days remaining from a given date to School Exam (22 Oct 2026).
     */
    fun getSchoolExamDaysRemaining(from: LocalDate = currentDate): Long {
        val days = ChronoUnit.DAYS.between(from, SCHOOL_EXAM_DATE)
        return maxOf(0L, days)
    }

    /**
     * Dynamically calculates days remaining from a given date to Board Exam (25 Feb 2027).
     */
    fun getBoardExamDaysRemaining(from: LocalDate = currentDate): Long {
        val days = ChronoUnit.DAYS.between(from, BOARD_EXAM_DATE)
        return maxOf(0L, days)
    }

    /**
     * Formats countdown text:
     * e.g., "School Exam — 32 days", on exam date "School Exam — EXAM DAY", never negative.
     */
    fun formatSchoolExamCountdown(from: LocalDate = currentDate): String {
        val days = getSchoolExamDaysRemaining(from)
        return when {
            days <= 0L -> "School Exam — EXAM DAY"
            days == 1L -> "School Exam — 1 day"
            else -> "School Exam — $days days"
        }
    }

    fun formatBoardExamCountdown(from: LocalDate = currentDate): String {
        val days = getBoardExamDaysRemaining(from)
        return when {
            days <= 0L -> "Board Exam — EXAM DAY"
            days == 1L -> "Board Exam — 1 day"
            else -> "Board Exam — $days days"
        }
    }

    /**
     * Core progress calculation engine.
     */
    private fun calculateDashboardState(
        completedIds: Set<String>,
        tasksList: List<StudyTask>,
        selectedPhase: ExamPhase?
    ): ExamDashboardState {
        val activePhase = selectedPhase ?: getActiveExamPhase(currentDate)
        val isSchoolPriority = currentDate.isBefore(SCHOOL_EXAM_DATE)

        // 1. Gather all chapters across all 7 subjects
        val allBoardChapters = mutableListOf<Chapter>()
        val allSchoolChapters = mutableListOf<Chapter>()

        val subjectProgress = mutableListOf<SubjectExamProgress>()

        StudyPlannerRepository.ORDERED_SUBJECT_IDS.forEach { subId ->
            val boardChapters = SyllabusRepository.getChapters(subId)
            val schoolChapters = SyllabusRepository.getChaptersByScope(subId, SyllabusScope.SCHOOL_EXAM)

            allBoardChapters.addAll(boardChapters)
            allSchoolChapters.addAll(schoolChapters)

            val schoolCompleted = schoolChapters.count { completedIds.contains(it.id) }
            val schoolTotal = schoolChapters.size
            val schoolPercent = if (schoolTotal > 0) (schoolCompleted.toFloat() / schoolTotal) * 100f else 0.0f

            val boardCompleted = boardChapters.count { completedIds.contains(it.id) }
            val boardTotal = boardChapters.size
            val boardPercent = if (boardTotal > 0) (boardCompleted.toFloat() / boardTotal) * 100f else 0.0f

            val subject = Subject.OFFICIAL_SUBJECTS.firstOrNull { it.id == subId }
            val gujaratiName = when (subId) {
                "accounts" -> "નામાનાં મૂળતત્ત્વો"
                "stat" -> "આંકડાશાસ્ત્ર"
                "economics" -> "અર્થશાસ્ત્ર"
                "ba" -> "વાણિજ્ય વ્યવસ્થા અને સંચાલન"
                "sp_cc" -> "એસ.પી. અને સી.સી."
                "english" -> "અંગ્રેજી (English)"
                "gujarati" -> "ગુજરાતી (પ્રથમ ભાષા)"
                else -> subject?.name ?: subId
            }

            subjectProgress.add(
                SubjectExamProgress(
                    subjectId = subId,
                    subjectName = subject?.name ?: subId,
                    gujaratiName = gujaratiName,
                    schoolExamCompleted = schoolCompleted,
                    schoolExamTotal = schoolTotal,
                    schoolExamPercent = schoolPercent,
                    boardExamCompleted = boardCompleted,
                    boardExamTotal = boardTotal,
                    boardExamPercent = boardPercent
                )
            )
        }

        // 2. Separate exam progress calculations
        val schoolCompletedCount = allSchoolChapters.count { completedIds.contains(it.id) }
        val schoolTotalCount = allSchoolChapters.size // 66 chapters
        val schoolProgressPercent = if (schoolTotalCount > 0) {
            (schoolCompletedCount.toFloat() / schoolTotalCount) * 100f
        } else 0.0f

        val boardCompletedCount = allBoardChapters.count { completedIds.contains(it.id) }
        val boardTotalCount = allBoardChapters.size // 94 chapters
        val boardProgressPercent = if (boardTotalCount > 0) {
            (boardCompletedCount.toFloat() / boardTotalCount) * 100f
        } else 0.0f

        // Overall progress reflects active phase or blended target towards 90+
        val overallProgressPercent = if (isSchoolPriority) {
            schoolProgressPercent
        } else {
            boardProgressPercent
        }

        // 3. Chapter pending and completed counts (for active phase scope)
        val targetScopeChapters = if (activePhase == ExamPhase.PHASE_1_SCHOOL) allSchoolChapters else allBoardChapters
        val completedCount = targetScopeChapters.count { completedIds.contains(it.id) }
        val pendingCount = targetScopeChapters.size - completedCount

        // 4. Missed tasks
        val missedTasks = tasksList.filter { it.isMissed && !it.isCompleted }

        return ExamDashboardState(
            targetMarks = AppConstants.DEFAULT_TARGET_MARKS,
            activePhase = activePhase,
            isSchoolExamPriority = isSchoolPriority,
            schoolExamDaysRemaining = getSchoolExamDaysRemaining(),
            schoolExamCountdownText = formatSchoolExamCountdown(),
            boardExamDaysRemaining = getBoardExamDaysRemaining(),
            boardExamCountdownText = formatBoardExamCountdown(),
            schoolExamCompletedCount = schoolCompletedCount,
            schoolExamTotalCount = schoolTotalCount,
            schoolExamProgressPercent = schoolProgressPercent,
            boardExamCompletedCount = boardCompletedCount,
            boardExamTotalCount = boardTotalCount,
            boardExamProgressPercent = boardProgressPercent,
            overallProgressPercent = overallProgressPercent,
            subjectProgressList = subjectProgress,
            completedChaptersCount = completedCount,
            pendingChaptersCount = pendingCount,
            missedTasksCount = missedTasks.size,
            missedTasks = missedTasks
        )
    }

    /**
     * Retrieve completed chapters for a given exam scope.
     */
    fun getCompletedChapters(scope: SyllabusScope): List<Chapter> {
        val completedIds = SyllabusRepository.completedChapterIds.value
        val chapters = mutableListOf<Chapter>()
        StudyPlannerRepository.ORDERED_SUBJECT_IDS.forEach { subId ->
            val list = SyllabusRepository.getChaptersByScope(subId, scope)
            chapters.addAll(list.filter { completedIds.contains(it.id) })
        }
        return chapters
    }

    /**
     * Retrieve pending chapters for a given exam scope.
     */
    fun getPendingChapters(scope: SyllabusScope): List<Chapter> {
        val completedIds = SyllabusRepository.completedChapterIds.value
        val chapters = mutableListOf<Chapter>()
        StudyPlannerRepository.ORDERED_SUBJECT_IDS.forEach { subId ->
            val list = SyllabusRepository.getChaptersByScope(subId, scope)
            chapters.addAll(list.filter { !completedIds.contains(it.id) })
        }
        return chapters
    }

    /**
     * Toggle chapter completion from Exam Dashboard.
     * Propagates to SyllabusRepository and StudyPlannerRepository.
     */
    fun toggleChapterCompletion(chapterId: String) {
        SyllabusRepository.toggleChapterCompletion(chapterId)
        refreshDashboardState()
    }

    /**
     * Reschedule missed task to today.
     */
    fun rescheduleMissedTask(taskId: String) {
        StudyPlannerRepository.rescheduleTask(taskId, StudyPlannerRepository.TODAY)
        refreshDashboardState()
    }

    /**
     * Reschedule all missed tasks to today.
     */
    fun rescheduleAllMissedToToday() {
        StudyPlannerRepository.rescheduleAllMissedToToday()
        refreshDashboardState()
    }

    /**
     * Toggle task completion.
     */
    fun toggleTaskCompletion(taskId: String) {
        StudyPlannerRepository.toggleTaskCompletion(taskId)
        refreshDashboardState()
    }
}
