package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.constants.AppConstants
import com.example.core.model.Chapter
import com.example.core.model.Subject
import com.example.core.model.SyllabusScope
import com.example.core.repository.SyllabusRepository
import com.example.ui.components.NexoraCard
import com.example.ui.components.NexoraTargetBadge
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraElectricBlue
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary

/**
 * Subject Detail Screen for Class 12 Commerce.
 * Displays official textbook chapters extracted directly from Gujarat State Board sources.
 *
 * Features:
 * - Full Board Exam syllabus vs School Exam (22 Oct 2026) distinct scoping.
 * - Part 1 and Part 2 separated with dedicated section headers.
 * - Original textbook terminology and vernacular naming strictly preserved.
 * - Interactive chapter checkboxes (chapter completion status).
 */
@Composable
fun SubjectDetailScreen(
    subject: Subject,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedScope by remember { mutableStateOf(SyllabusScope.BOARD_EXAM) }
    val completedChapterIds by SyllabusRepository.completedChapterIds.collectAsStateWithLifecycle()

    val chapters = remember(subject.id, selectedScope) {
        SyllabusRepository.getChaptersByScope(subject.id, selectedScope)
    }

    val completedCount = chapters.count { completedChapterIds.contains(it.id) }
    val totalCount = chapters.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0.0f

    // Check if chapters are split into Part 1 & Part 2
    val hasParts = chapters.any { it.part != null }
    val groupedByPart = remember(chapters) {
        if (hasParts) chapters.groupBy { it.partName } else emptyMap()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NexoraBackground)
            .statusBarsPadding()
            .testTag("subject_detail_screen_${subject.id}")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Back Button & Subject Name
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("subject_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Subjects",
                            tint = NexoraTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = subject.name,
                            color = NexoraTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Code: ${subject.code} • Class 12 Commerce (GSEB)",
                            color = NexoraTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Target Marks Highlight
            item {
                NexoraTargetBadge(
                    modifier = Modifier.fillMaxWidth(),
                    targetText = "90+ MARKS",
                    subtitle = "Official GSEB Syllabus Target"
                )
            }

            // Syllabus Scope Toggle (Board Exam vs School Exam 22 Oct 2026)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Syllabus Milestone",
                        color = NexoraTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NexoraSurfaceElevated, shape = RoundedCornerShape(12.dp))
                            .border(1.dp, NexoraBorder, shape = RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Board Exam Tab
                        ScopeTabButton(
                            title = "Full Board Exam",
                            subtitle = "100% Syllabus",
                            isSelected = selectedScope == SyllabusScope.BOARD_EXAM,
                            testTag = "scope_tab_board",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedScope = SyllabusScope.BOARD_EXAM }
                        )

                        // School Exam Tab
                        ScopeTabButton(
                            title = "School Exam",
                            subtitle = "22 Oct 2026",
                            isSelected = selectedScope == SyllabusScope.SCHOOL_EXAM,
                            testTag = "scope_tab_school",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedScope = SyllabusScope.SCHOOL_EXAM }
                        )
                    }
                }
            }

            // Syllabus Scope Banner
            item {
                if (selectedScope == SyllabusScope.SCHOOL_EXAM) {
                    NexoraCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = NexoraElectricBlue.copy(alpha = 0.15f),
                        borderColor = NexoraCyan.copy(alpha = 0.4f),
                        contentPadding = 14.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NexoraCyan.copy(alpha = 0.2f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Event,
                                    contentDescription = null,
                                    tint = NexoraCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "School Exam Syllabus • ${AppConstants.SCHOOL_EXAM_DATE}",
                                    color = NexoraCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Targeted mapping for the upcoming 22 October examination.",
                                    color = NexoraTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    NexoraCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = NexoraSurfaceElevated.copy(alpha = 0.7f),
                        borderColor = NexoraBorder,
                        contentPadding = 14.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NexoraGold.copy(alpha = 0.15f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.School,
                                    contentDescription = null,
                                    tint = NexoraGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Full GSEB Board Syllabus",
                                    color = NexoraTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Comprehensive curriculum mapped to the 90+ Marks standard.",
                                    color = NexoraTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Chapter Progress Summary Card
            item {
                NexoraCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 16.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AssignmentTurnedIn,
                                    contentDescription = null,
                                    tint = NexoraCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Chapter Progress",
                                    color = NexoraTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "$completedCount / $totalCount Completed",
                                color = if (completedCount > 0) NexoraCyan else NexoraTextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = NexoraCyan,
                            trackColor = NexoraSurfaceVariant
                        )

                        Text(
                            text = "${(progressFraction * 100).toInt()}% of ${if (selectedScope == SyllabusScope.SCHOOL_EXAM) "School Exam" else "Full Board"} syllabus covered",
                            color = NexoraTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedScope == SyllabusScope.SCHOOL_EXAM) "School Exam Chapters" else "All Chapters & Units",
                        color = NexoraTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalCount chapters",
                        color = NexoraTextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Render Chapters: Grouped by Part if applicable, or single list
            if (hasParts && groupedByPart.isNotEmpty()) {
                groupedByPart.forEach { (partName, partChapters) ->
                    item {
                        PartHeader(partName = partName ?: "Part")
                    }

                    items(partChapters, key = { it.id }) { chapter ->
                        ChapterItemCard(
                            chapter = chapter,
                            isCompleted = completedChapterIds.contains(chapter.id),
                            showSchoolBadge = selectedScope == SyllabusScope.BOARD_EXAM && chapter.isInSchoolExam,
                            onToggleCompletion = {
                                SyllabusRepository.toggleChapterCompletion(chapter.id)
                            }
                        )
                    }
                }
            } else {
                items(chapters, key = { it.id }) { chapter ->
                    ChapterItemCard(
                        chapter = chapter,
                        isCompleted = completedChapterIds.contains(chapter.id),
                        showSchoolBadge = selectedScope == SyllabusScope.BOARD_EXAM && chapter.isInSchoolExam,
                        onToggleCompletion = {
                            SyllabusRepository.toggleChapterCompletion(chapter.id)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Toggle button inside the syllabus scope selector.
 */
@Composable
private fun ScopeTabButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) NexoraCyan.copy(alpha = 0.2f) else Color.Transparent
            )
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) NexoraCyan else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = if (isSelected) NexoraCyan else NexoraTextSecondary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = if (isSelected) NexoraTextPrimary else NexoraTextMuted,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Visual header separating Part 1 and Part 2.
 */
@Composable
private fun PartHeader(
    partName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
            .background(NexoraSurfaceElevated, shape = RoundedCornerShape(8.dp))
            .border(1.dp, NexoraBorder, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(NexoraCyan, shape = CircleShape)
            )
            Text(
                text = partName,
                color = NexoraCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Card representing an individual official textbook chapter with an interactive checkbox.
 */
@Composable
private fun ChapterItemCard(
    chapter: Chapter,
    isCompleted: Boolean,
    showSchoolBadge: Boolean,
    onToggleCompletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    NexoraCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chapter_item_${chapter.id}"),
        containerColor = if (isCompleted) NexoraSurfaceElevated.copy(alpha = 0.5f) else NexoraSurface,
        borderColor = if (isCompleted) NexoraCyan.copy(alpha = 0.3f) else NexoraBorder,
        contentPadding = 12.dp,
        onClick = onToggleCompletion
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accessible Checkbox (min 48dp touch target)
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onToggleCompletion() },
                modifier = Modifier.testTag("chapter_checkbox_${chapter.id}"),
                colors = CheckboxDefaults.colors(
                    checkedColor = NexoraCyan,
                    uncheckedColor = NexoraBorder,
                    checkmarkColor = NexoraBackground
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Chapter Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = chapter.title,
                    color = if (isCompleted) NexoraTextMuted else NexoraTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    lineHeight = 20.sp
                )

                if (chapter.englishTitle != null && chapter.englishTitle != chapter.title) {
                    Text(
                        text = chapter.englishTitle,
                        color = NexoraTextSecondary,
                        fontSize = 11.sp
                    )
                }

                if (showSchoolBadge) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(NexoraCyan.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "School Exam: 22 Oct",
                            color = NexoraCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
