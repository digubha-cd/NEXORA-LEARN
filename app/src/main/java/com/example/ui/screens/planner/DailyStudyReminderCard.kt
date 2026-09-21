package com.example.ui.screens.planner

import android.app.TimePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.StudyTask
import com.example.core.reminder.DailyReminderConfig
import com.example.core.reminder.ReminderSlot
import com.example.core.reminder.StudyReminderManager
import com.example.ui.components.NexoraCard
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraElectricBlue
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraSuccess
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary

/**
 * Card for configuring and managing the Daily Study Session Notification Scheduling system.
 * Reminds students of their planned study sessions for the day.
 */
@Composable
fun DailyStudyReminderCard(
    todayTasks: List<StudyTask>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var reminderConfig by remember {
        mutableStateOf(StudyReminderManager.getDailyStudyReminderPreferences(context))
    }

    var selectedHour by remember { mutableIntStateOf(reminderConfig.hour) }
    var selectedMinute by remember { mutableIntStateOf(reminderConfig.minute) }
    var isEnabled by remember { mutableStateOf(reminderConfig.enabled) }

    val pendingTodayCount = remember(todayTasks) { todayTasks.count { !it.isCompleted } }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                isEnabled = true
                StudyReminderManager.setDailyStudyReminderPreferences(
                    context = context,
                    enabled = true,
                    hour = hourOfDay,
                    minute = minute
                )
                reminderConfig = DailyReminderConfig(true, hourOfDay, minute)
                Toast.makeText(
                    context,
                    "Daily reminder set for ${reminderConfig.formattedTime}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            selectedHour,
            selectedMinute,
            false
        )
    }

    NexoraCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_study_reminder_card"),
        contentPadding = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header with Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isEnabled) NexoraCyan.copy(alpha = 0.15f) else NexoraSurfaceElevated)
                            .border(
                                1.dp,
                                if (isEnabled) NexoraCyan.copy(alpha = 0.4f) else NexoraBorder,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEnabled) Icons.Outlined.NotificationsActive else Icons.Outlined.NotificationsOff,
                            contentDescription = "Daily Study Reminder",
                            tint = if (isEnabled) NexoraCyan else NexoraTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Daily Study Plan Reminder",
                            color = NexoraTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEnabled) "Active at ${reminderConfig.formattedTime}" else "Disabled",
                            color = if (isEnabled) NexoraCyan else NexoraTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { checked ->
                        isEnabled = checked
                        StudyReminderManager.setDailyStudyReminderPreferences(
                            context = context,
                            enabled = checked,
                            hour = selectedHour,
                            minute = selectedMinute
                        )
                        reminderConfig = DailyReminderConfig(checked, selectedHour, selectedMinute)
                        val msg = if (checked) {
                            "Daily study reminder enabled for ${reminderConfig.formattedTime}"
                        } else {
                            "Daily study reminder disabled"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = NexoraCyan,
                        uncheckedThumbColor = NexoraTextMuted,
                        uncheckedTrackColor = NexoraSurfaceElevated
                    ),
                    modifier = Modifier.testTag("daily_reminder_switch")
                )
            }

            // Description / Status Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NexoraBackground.copy(alpha = 0.6f))
                    .border(1.dp, NexoraBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (pendingTodayCount > 0) Icons.Outlined.Notifications else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = if (pendingTodayCount > 0) NexoraGold else NexoraSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (pendingTodayCount > 0) {
                            "$pendingTodayCount planned study session(s) scheduled for today. Target: 90+ Marks"
                        } else if (todayTasks.isNotEmpty()) {
                            "All today's planned study sessions are completed! 🎉"
                        } else {
                            "Reminds you of all study sessions planned for the day every morning/evening."
                        },
                        color = NexoraTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Quick Slots Selector
            if (isEnabled) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Preferred Reminder Time",
                        color = NexoraTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Morning Slot (7:00 AM)
                        val isMorningSelected = selectedHour == 7 && selectedMinute == 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isMorningSelected) NexoraCyan.copy(alpha = 0.2f) else NexoraSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isMorningSelected) NexoraCyan else NexoraBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedHour = 7
                                    selectedMinute = 0
                                    StudyReminderManager.setDailyStudyReminderPreferences(
                                        context = context,
                                        enabled = true,
                                        hour = 7,
                                        minute = 0
                                    )
                                    reminderConfig = DailyReminderConfig(true, 7, 0)
                                    Toast.makeText(context, "Set to Morning (7:00 AM)", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Morning",
                                    color = if (isMorningSelected) NexoraCyan else NexoraTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "7:00 AM",
                                    color = if (isMorningSelected) NexoraTextPrimary else NexoraTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Evening Slot (5:00 PM)
                        val isEveningSelected = selectedHour == 17 && selectedMinute == 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isEveningSelected) NexoraPurple.copy(alpha = 0.2f) else NexoraSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isEveningSelected) NexoraPurple else NexoraBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedHour = 17
                                    selectedMinute = 0
                                    StudyReminderManager.setDailyStudyReminderPreferences(
                                        context = context,
                                        enabled = true,
                                        hour = 17,
                                        minute = 0
                                    )
                                    reminderConfig = DailyReminderConfig(true, 17, 0)
                                    Toast.makeText(context, "Set to Evening (5:00 PM)", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Evening",
                                    color = if (isEveningSelected) NexoraPurple else NexoraTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "5:00 PM",
                                    color = if (isEveningSelected) NexoraTextPrimary else NexoraTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Night Slot (9:00 PM)
                        val isNightSelected = selectedHour == 21 && selectedMinute == 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isNightSelected) NexoraElectricBlue.copy(alpha = 0.2f) else NexoraSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isNightSelected) NexoraElectricBlue else NexoraBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedHour = 21
                                    selectedMinute = 0
                                    StudyReminderManager.setDailyStudyReminderPreferences(
                                        context = context,
                                        enabled = true,
                                        hour = 21,
                                        minute = 0
                                    )
                                    reminderConfig = DailyReminderConfig(true, 21, 0)
                                    Toast.makeText(context, "Set to Night (9:00 PM)", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Night",
                                    color = if (isNightSelected) NexoraElectricBlue else NexoraTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "9:00 PM",
                                    color = if (isNightSelected) NexoraTextPrimary else NexoraTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Custom Time Picker Button
                        val isCustom = !(isMorningSelected || isEveningSelected || isNightSelected)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCustom) NexoraGold.copy(alpha = 0.2f) else NexoraSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isCustom) NexoraGold else NexoraBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    timePickerDialog.show()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Custom",
                                    color = if (isCustom) NexoraGold else NexoraTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isCustom) reminderConfig.formattedTime else "Pick Time",
                                    color = if (isCustom) NexoraTextPrimary else NexoraTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Actions row: Test Reminder preview button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        StudyReminderManager.sendImmediateDailyStudySessionNotification(context)
                        Toast.makeText(
                            context,
                            "Notification triggered! Check your notification shade.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.testTag("test_daily_reminder_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = null,
                        tint = NexoraCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Send Test Reminder",
                        color = NexoraCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
