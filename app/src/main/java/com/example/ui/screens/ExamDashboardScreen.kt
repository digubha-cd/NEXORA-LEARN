package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.constants.AppConstants
import com.example.core.model.Chapter
import com.example.core.model.ExamDashboardState
import com.example.core.model.ExamPhase
import com.example.core.model.StudyTask
import com.example.core.model.Subject
import com.example.core.model.SubjectExamProgress
import com.example.core.model.SyllabusScope
import com.example.core.repository.ExamRepository
import com.example.core.repository.SyllabusRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NexoraCard
import com.example.ui.components.NexoraLogo
import com.example.ui.components.NexoraTargetBadge
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraBorderGlow
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraElectricBlue
import com.example.ui.theme.NexoraError
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraMagenta
import com.example.ui.theme.NexoraPink
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraSuccess
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary
import com.example.ui.theme.NexoraWarning
import java.util.Locale

private enum class ChapterListFilter {
    PENDING_CHAPTERS,
    COMPLETED_CHAPTERS,
    MISSED_TASKS
}

/**
 * Exam System Dashboard:
 * 1. Two separate exam phases:
 *    - School Exam (22 October 2026): Phase 1 Priority (66 chapters).
 *    - Board Exam (25 February 2027): Phase 2 Priority (94 chapters, complete syllabus).
 * 2. Dynamic Exam Countdowns ("School Exam — X days remaining", "Board Exam — Y days remaining").
 * 3. 90+ Marks Target Tracking & Overall preparation percentage.
 * 4. Exam-wise and Subject-wise preparation percentage breakdown for all 7 subjects.
 * 5. Completed chapters, pending chapters, and missed study tasks with one-tap reschedule.
 * 6. Two-way reactive connection to chapter checkboxes & Study Planner tasks.
 */
@Composable
fun ExamDashboardScreen(
    onSubjectClick: (Subject) -> Unit = {},
    onNavigateToPlanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dashboardState by ExamRepository.dashboardState.collectAsStateWithLifecycle()
    val completedChapterIds by SyllabusRepository.completedChapterIds.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf(ChapterListFilter.PENDING_CHAPTERS) }

    val currentScope = if (dashboardState.activePhase == ExamPhase.PHASE_1_SCHOOL) {
        SyllabusScope.SCHOOL_EXAM
    } else {
        SyllabusScope.BOARD_EXAM
    }

    val pendingChapters = remember(completedChapterIds, currentScope) {
        ExamRepository.getPendingChapters(currentScope)
    }

    val completedChapters = remember(completedChapterIds, currentScope) {
        ExamRepository.getCompletedChapters(currentScope)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NexoraBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("exam_dashboard_screen_root")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header with branding
            item {
                ExamHeader(dashboardState = dashboardState)
            }

            // 2. MAIN TARGET: 90+ MARKS TARGET (Clean, prominent full-width card)
            item {
                NexoraTargetBadge(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exam_target_badge"),
                    targetTitle = "90+ MARKS TARGET",
                    subtitle = "Gujarat Board • Class 12 Commerce",
                    compact = false
                )
            }

            // 3. Phase Priority Banner (School Exam Priority vs Board Exam)
            item {
                ExamPhasePriorityBanner(dashboardState = dashboardState)
            }

            // 3. Dynamic Exam Countdowns (School Exam and Board Exam)
            item {
                ExamCountdownSection(dashboardState = dashboardState)
            }

            // 4. Overall & Exam-Wise Preparation Progress
            item {
                ExamProgressOverviewSection(dashboardState = dashboardState)
            }

            // 5. Quick Stats Grid (Completed, Pending, Missed, Target)
            item {
                ExamQuickStatsGrid(
                    dashboardState = dashboardState,
                    selectedFilter = selectedFilter,
                    onSelectFilter = { selectedFilter = it }
                )
            }

            // 6. Subject-Wise Preparation Breakdown (All 7 subjects)
            item {
                SubjectWisePreparationSection(
                    subjectProgressList = dashboardState.subjectProgressList,
                    onSubjectClick = onSubjectClick
                )
            }

            // 7. Interactive Breakdown Header & Filter Tabs
            item {
                ExamBreakdownHeader(
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it },
                    completedCount = dashboardState.completedChaptersCount,
                    pendingCount = dashboardState.pendingChaptersCount,
                    missedCount = dashboardState.missedTasksCount,
                    onRescheduleAllMissed = { ExamRepository.rescheduleAllMissedToToday() }
                )
            }

            // 8. Dynamic Content based on selected filter
            when (selectedFilter) {
                ChapterListFilter.PENDING_CHAPTERS -> {
                    if (pendingChapters.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "All Chapters Completed!",
                                subtitle = "Outstanding work! You have completed all syllabus chapters for this exam milestone.",
                                icon = Icons.Outlined.CheckCircle,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }
                    } else {
                        items(
                            items = pendingChapters,
                            key = { it.id }
                        ) { chapter ->
                            ChapterItemCard(
                                chapter = chapter,
                                isCompleted = false,
                                onToggle = { ExamRepository.toggleChapterCompletion(chapter.id) }
                            )
                        }
                    }
                }

                ChapterListFilter.COMPLETED_CHAPTERS -> {
                    if (completedChapters.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No Chapters Completed Yet",
                                subtitle = "Check off chapters as you study to watch your 90+ preparation progress climb!",
                                icon = Icons.Filled.HourglassBottom,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }
                    } else {
                        items(
                            items = completedChapters,
                            key = { it.id }
                        ) { chapter ->
                            ChapterItemCard(
                                chapter = chapter,
                                isCompleted = true,
                                onToggle = { ExamRepository.toggleChapterCompletion(chapter.id) }
                            )
                        }
                    }
                }

                ChapterListFilter.MISSED_TASKS -> {
                    val missedTasks = dashboardState.missedTasks

                    if (missedTasks.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No Missed Tasks",
                                subtitle = "Your daily study schedule is fully on track towards the 90+ Marks target.",
                                icon = Icons.Outlined.CheckCircle,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }
                    } else {
                        items(
                            items = missedTasks,
                            key = { it.taskId }
                        ) { task ->
                            MissedTaskItemCard(
                                task = task,
                                onReschedule = { ExamRepository.rescheduleMissedTask(task.taskId) },
                                onToggle = { ExamRepository.toggleTaskCompletion(task.taskId) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Sub-Composables
// ----------------------------------------------------------------------------

@Composable
private fun ExamHeader(
    dashboardState: ExamDashboardState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exam_header_row"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NexoraLogo(
                emblemSize = 38.dp,
                showWordmark = false,
                showSubtitle = false
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Exam Dashboard",
                    color = NexoraTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Class 12 Commerce • Gujarat Board",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Phase Status Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (dashboardState.isSchoolExamPriority) NexoraCyan.copy(alpha = 0.15f)
                    else NexoraMagenta.copy(alpha = 0.15f)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            if (dashboardState.isSchoolExamPriority) NexoraCyan else NexoraMagenta,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (dashboardState.isSchoolExamPriority) "Phase 1 Active" else "Phase 2 Active",
                    color = if (dashboardState.isSchoolExamPriority) NexoraCyan else NexoraMagenta,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ExamPhasePriorityBanner(
    dashboardState: ExamDashboardState,
    modifier: Modifier = Modifier
) {
    val isPhase1 = dashboardState.isSchoolExamPriority
    val borderColor = if (isPhase1) NexoraCyan else NexoraMagenta
    val badgeBg = if (isPhase1) NexoraCyan.copy(alpha = 0.15f) else NexoraMagenta.copy(alpha = 0.15f)
    val textColor = if (isPhase1) NexoraCyan else NexoraMagenta

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NexoraSurfaceElevated)
            .border(1.dp, borderColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("exam_phase_priority_banner")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(badgeBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPhase1) Icons.Filled.School else Icons.Outlined.Stars,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPhase1) "Phase 1: School Exam Priority" else "Phase 2: Board Exam Priority",
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPhase1) "22 Oct 2026" else "25 Feb 2027",
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isPhase1) {
                    "Currently prioritizing School Exam syllabus (66 chapters across all 7 subjects). Board Exam exclusive chapters will remain isolated until School Exam completion on 22 October 2026."
                } else {
                    "School Exam phase completed. Full study focus is now automatically allocated to the complete Board Exam syllabus (all 94 chapters across 7 subjects)."
                },
                color = NexoraTextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun ExamCountdownSection(
    dashboardState: ExamDashboardState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Exam Countdowns",
            color = NexoraTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // School Exam Countdown Card (Phase 1)
            NexoraCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("school_exam_countdown_card"),
                contentPadding = 12.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(NexoraCyan.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = null,
                            tint = NexoraCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NexoraCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Phase 1",
                            color = NexoraCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "School Exam",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = AppConstants.SCHOOL_EXAM_DATE,
                    color = NexoraCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NexoraCyan.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = dashboardState.schoolExamCountdownText,
                        color = NexoraCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 15.sp,
                        modifier = Modifier.testTag("school_exam_countdown_text")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "66 Chapters • School Syllabus",
                    color = NexoraTextMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Board Exam Countdown Card (Phase 2)
            NexoraCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("board_exam_countdown_card"),
                contentPadding = 12.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(NexoraMagenta.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Stars,
                            contentDescription = null,
                            tint = NexoraMagenta,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NexoraMagenta.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Phase 2",
                            color = NexoraMagenta,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Board Exam",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = AppConstants.BOARD_EXAM_DATE,
                    color = NexoraMagenta,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NexoraMagenta.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = dashboardState.boardExamCountdownText,
                        color = NexoraMagenta,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 15.sp,
                        modifier = Modifier.testTag("board_exam_countdown_text")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "94 Chapters • 100% Target",
                    color = NexoraTextMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ExamProgressOverviewSection(
    dashboardState: ExamDashboardState,
    modifier: Modifier = Modifier
) {
    NexoraCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exam_progress_overview_card"),
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Preparation Progress",
                    color = NexoraTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dual-Phase Class 12 Syllabus Tracking",
                    color = NexoraTextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NexoraGold.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "90+ Target",
                    color = NexoraGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Phase 1: School Exam Progress Bar
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "School Exam Syllabus (Phase 1)",
                    color = NexoraCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${dashboardState.schoolExamCompletedCount} / ${dashboardState.schoolExamTotalCount} Chapters (${String.format(Locale.ENGLISH, "%.1f", dashboardState.schoolExamProgressPercent)}%)",
                    color = NexoraTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (dashboardState.schoolExamProgressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .testTag("school_exam_progress_bar"),
                color = NexoraCyan,
                trackColor = NexoraSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Phase 2: Board Exam Progress Bar
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Board Exam Syllabus (Phase 2)",
                    color = NexoraMagenta,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${dashboardState.boardExamCompletedCount} / ${dashboardState.boardExamTotalCount} Chapters (${String.format(Locale.ENGLISH, "%.1f", dashboardState.boardExamProgressPercent)}%)",
                    color = NexoraTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (dashboardState.boardExamProgressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .testTag("board_exam_progress_bar"),
                color = NexoraMagenta,
                trackColor = NexoraSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Overall Preparation Metric
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NexoraSurfaceVariant)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Overall Preparation (Active Phase)",
                        color = NexoraTextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${String.format(Locale.ENGLISH, "%.1f", dashboardState.overallProgressPercent)}% Ready",
                        color = if (dashboardState.overallProgressPercent >= 90f) NexoraSuccess else NexoraCyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (dashboardState.isSchoolExamPriority) "Focus: 22 Oct 2026" else "Focus: 25 Feb 2027",
                    color = NexoraTextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ExamQuickStatsGrid(
    dashboardState: ExamDashboardState,
    selectedFilter: ChapterListFilter,
    onSelectFilter: (ChapterListFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Completed Chapters
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Completed",
            value = "${dashboardState.completedChaptersCount}",
            valueColor = NexoraSuccess,
            isSelected = selectedFilter == ChapterListFilter.COMPLETED_CHAPTERS,
            onClick = { onSelectFilter(ChapterListFilter.COMPLETED_CHAPTERS) },
            testTag = "stat_completed_chapters"
        )

        // Pending Chapters
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Pending",
            value = "${dashboardState.pendingChaptersCount}",
            valueColor = NexoraCyan,
            isSelected = selectedFilter == ChapterListFilter.PENDING_CHAPTERS,
            onClick = { onSelectFilter(ChapterListFilter.PENDING_CHAPTERS) },
            testTag = "stat_pending_chapters"
        )

        // Missed Tasks
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Missed",
            value = "${dashboardState.missedTasksCount}",
            valueColor = if (dashboardState.missedTasksCount > 0) NexoraError else NexoraTextSecondary,
            isSelected = selectedFilter == ChapterListFilter.MISSED_TASKS,
            onClick = { onSelectFilter(ChapterListFilter.MISSED_TASKS) },
            testTag = "stat_missed_tasks"
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    valueColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val borderColor = if (isSelected) valueColor else NexoraBorder
    val bgColor = if (isSelected) NexoraSurfaceElevated else NexoraSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .testTag(testTag)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = value,
                color = valueColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = NexoraTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SubjectWisePreparationSection(
    subjectProgressList: List<SubjectExamProgress>,
    onSubjectClick: (Subject) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    NexoraCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("subject_wise_preparation_card"),
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Subject-Wise Preparation",
                    color = NexoraTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "7 Gujarat Board Class 12 Commerce Subjects",
                    color = NexoraTextSecondary,
                    fontSize = 11.sp
                )
            }

            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.testTag("toggle_expand_subjects_button")
            ) {
                Text(
                    text = if (isExpanded) "Show Less" else "View All (7)",
                    color = NexoraCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Display 3 by default or all 7 when expanded
        val itemsToShow = if (isExpanded) subjectProgressList else subjectProgressList.take(3)

        Column(
            modifier = Modifier.animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsToShow.forEach { progress ->
                val subject = Subject.OFFICIAL_SUBJECTS.firstOrNull { it.id == progress.subjectId }
                SubjectProgressItem(
                    progress = progress,
                    onClick = {
                        if (subject != null) onSubjectClick(subject)
                    }
                )
            }
        }
    }
}

@Composable
private fun SubjectProgressItem(
    progress: SubjectExamProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(NexoraSurfaceVariant)
            .border(1.dp, NexoraBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("subject_progress_item_${progress.subjectId}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = progress.subjectName,
                        color = NexoraTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = progress.gujaratiName,
                        color = NexoraTextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${String.format(Locale.ENGLISH, "%.0f", progress.schoolExamPercent)}% School",
                        color = NexoraCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        color = NexoraTextMuted,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${String.format(Locale.ENGLISH, "%.0f", progress.boardExamPercent)}% Board",
                        color = NexoraMagenta,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // School Exam mini progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "School:",
                    color = NexoraTextSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.width(42.dp)
                )
                LinearProgressIndicator(
                    progress = { (progress.schoolExamPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NexoraCyan,
                    trackColor = NexoraSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${progress.schoolExamCompleted}/${progress.schoolExamTotal}",
                    color = NexoraTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Board Exam mini progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Board:",
                    color = NexoraTextSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.width(42.dp)
                )
                LinearProgressIndicator(
                    progress = { (progress.boardExamPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NexoraMagenta,
                    trackColor = NexoraSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${progress.boardExamCompleted}/${progress.boardExamTotal}",
                    color = NexoraTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ExamBreakdownHeader(
    selectedFilter: ChapterListFilter,
    onFilterChange: (ChapterListFilter) -> Unit,
    completedCount: Int,
    pendingCount: Int,
    missedCount: Int,
    onRescheduleAllMissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chapter & Task Status",
                color = NexoraTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            if (selectedFilter == ChapterListFilter.MISSED_TASKS && missedCount > 0) {
                Button(
                    onClick = onRescheduleAllMissed,
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraCyan.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("reschedule_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = NexoraCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Reschedule All",
                        color = NexoraCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips - symmetric weight for 3 items with no clipping
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == ChapterListFilter.PENDING_CHAPTERS,
                onClick = { onFilterChange(ChapterListFilter.PENDING_CHAPTERS) },
                label = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Pending ($pendingCount)", fontSize = 11.sp, maxLines = 1)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NexoraCyan.copy(alpha = 0.2f),
                    selectedLabelColor = NexoraCyan,
                    containerColor = NexoraSurface,
                    labelColor = NexoraTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == ChapterListFilter.PENDING_CHAPTERS,
                    borderColor = NexoraBorder,
                    selectedBorderColor = NexoraCyan
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("filter_pending_chip")
            )

            FilterChip(
                selected = selectedFilter == ChapterListFilter.COMPLETED_CHAPTERS,
                onClick = { onFilterChange(ChapterListFilter.COMPLETED_CHAPTERS) },
                label = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Done ($completedCount)", fontSize = 11.sp, maxLines = 1)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NexoraSuccess.copy(alpha = 0.2f),
                    selectedLabelColor = NexoraSuccess,
                    containerColor = NexoraSurface,
                    labelColor = NexoraTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == ChapterListFilter.COMPLETED_CHAPTERS,
                    borderColor = NexoraBorder,
                    selectedBorderColor = NexoraSuccess
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("filter_completed_chip")
            )

            FilterChip(
                selected = selectedFilter == ChapterListFilter.MISSED_TASKS,
                onClick = { onFilterChange(ChapterListFilter.MISSED_TASKS) },
                label = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Missed ($missedCount)", fontSize = 11.sp, maxLines = 1)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NexoraError.copy(alpha = 0.2f),
                    selectedLabelColor = NexoraError,
                    containerColor = NexoraSurface,
                    labelColor = NexoraTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilter == ChapterListFilter.MISSED_TASKS,
                    borderColor = NexoraBorder,
                    selectedBorderColor = NexoraError
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("filter_missed_chip")
            )
        }
    }
}

@Composable
private fun ChapterItemCard(
    chapter: Chapter,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subject = Subject.OFFICIAL_SUBJECTS.firstOrNull { it.id == chapter.subjectId }
    val borderColor = if (isCompleted) NexoraSuccess.copy(alpha = 0.4f) else NexoraBorder

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NexoraSurface)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .padding(12.dp)
            .testTag("exam_chapter_card_${chapter.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = NexoraSuccess,
                    uncheckedColor = NexoraCyan,
                    checkmarkColor = NexoraBackground
                ),
                modifier = Modifier.testTag("checkbox_${chapter.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = subject?.name ?: chapter.subjectId,
                        color = NexoraCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (chapter.isInSchoolExam) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NexoraCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "School Exam",
                                color = NexoraCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NexoraMagenta.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Board Only",
                                color = NexoraMagenta,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = chapter.title,
                    color = if (isCompleted) NexoraTextSecondary else NexoraTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                if (!chapter.englishTitle.isNullOrEmpty()) {
                    Text(
                        text = chapter.englishTitle,
                        color = NexoraTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MissedTaskItemCard(
    task: StudyTask,
    onReschedule: () -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NexoraSurface)
            .border(1.dp, NexoraError.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("exam_missed_task_card_${task.taskId}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(NexoraError.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = NexoraError,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.subjectName,
                        color = NexoraError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Scheduled: ${task.scheduledDateFormatted}",
                    color = NexoraTextMuted,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = task.chapterTitle,
                color = NexoraTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Duration: ${task.durationMinutes} mins",
                    color = NexoraTextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "Priority: ${task.priority.name}",
                    color = NexoraCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onReschedule,
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraCyan),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("reschedule_btn_${task.taskId}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = NexoraBackground,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Reschedule to Today",
                        color = NexoraBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraSuccess),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(0.6f)
                        .height(36.dp)
                        .testTag("mark_done_btn_${task.taskId}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = NexoraBackground,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Done",
                        color = NexoraBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
