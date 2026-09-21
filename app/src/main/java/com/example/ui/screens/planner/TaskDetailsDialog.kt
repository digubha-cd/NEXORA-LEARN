package com.example.ui.screens.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.ExamType
import com.example.core.model.StudyTask
import com.example.core.model.TaskStatus
import com.example.core.repository.StudyPlannerRepository
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraError
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraMagenta
import com.example.ui.theme.NexoraSuccess
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary
import java.time.LocalDate

/**
 * Task Details & Actions Dialog.
 * Allows student to:
 * - View full chapter details and planned date
 * - Mark Complete / Mark Incomplete
 * - Reschedule (Today, Tomorrow, +3 Days, +7 Days)
 * - Mark / handle Missed status
 */
@Composable
fun TaskDetailsDialog(
    task: StudyTask,
    onDismiss: () -> Unit,
    onToggleComplete: () -> Unit,
    onReschedule: (LocalDate) -> Unit,
    onToggleMissed: (Boolean) -> Unit
) {
    val statusColor = when (task.status) {
        TaskStatus.COMPLETED -> NexoraSuccess
        TaskStatus.MISSED -> NexoraError
        TaskStatus.PENDING -> NexoraCyan
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("task_details_dialog"),
        containerColor = NexoraSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = task.status.label.uppercase(),
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = task.subjectName,
                        color = NexoraTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = NexoraTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Chapter Title
                Column {
                    Text(
                        text = "Chapter",
                        color = NexoraTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.chapterTitle,
                        color = NexoraTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                }

                // Exam Phase, Daily Duration & Planned Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Exam Phase",
                            color = NexoraTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (task.examPhase == ExamType.SCHOOL_EXAM) "School Exam (22 Oct)" else "Board Exam (25 Feb)",
                            color = if (task.examPhase == ExamType.SCHOOL_EXAM) NexoraCyan else NexoraMagenta,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Daily Duration",
                            color = NexoraTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.durationFormatted,
                            color = NexoraGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Planned Date",
                            color = NexoraTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.scheduledDateFormatted,
                            color = if (task.isMissed) NexoraError else NexoraTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (task.rescheduled) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = NexoraGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "This chapter task was previously rescheduled.",
                            color = NexoraGold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action: Complete / Incomplete
                Button(
                    onClick = {
                        onToggleComplete()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_action_toggle_complete"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.isCompleted) NexoraSurfaceElevated else NexoraSuccess,
                        contentColor = if (task.isCompleted) NexoraTextPrimary else NexoraBackground
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (task.isCompleted) "Mark as Pending / Incomplete" else "Mark Complete ✓",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Reschedule Options
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Reschedule Task",
                        color = NexoraTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onReschedule(StudyPlannerRepository.TODAY)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_reschedule_to_today"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NexoraCyan.copy(alpha = 0.2f),
                                contentColor = NexoraCyan
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Today", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onReschedule(StudyPlannerRepository.TODAY.plusDays(1))
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_reschedule_to_tomorrow"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NexoraSurfaceElevated,
                                contentColor = NexoraCyan
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Tomorrow", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                onReschedule(StudyPlannerRepository.TODAY.plusDays(3))
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NexoraSurfaceElevated,
                                contentColor = NexoraCyan
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+3 Days", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Mark/Handle Missed Option
                if (!task.isCompleted) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (task.isMissed) "Status: Missed" else "Mark as Missed?",
                            color = if (task.isMissed) NexoraError else NexoraTextMuted,
                            fontSize = 11.sp
                        )

                        TextButton(
                            onClick = {
                                onToggleMissed(!task.isMissed)
                                onDismiss()
                            },
                            modifier = Modifier.testTag("dialog_toggle_missed")
                        ) {
                            Text(
                                text = if (task.isMissed) "Clear Missed Status" else "Mark Missed",
                                color = if (task.isMissed) NexoraCyan else NexoraError,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
