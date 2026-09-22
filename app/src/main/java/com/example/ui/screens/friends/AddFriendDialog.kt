package com.example.ui.screens.friends

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.util.StudentIdGenerator
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary

/**
 * Add / Invite Friend Dialog.
 * Searches and sends study friend invites exclusively by unique NEXORA Student ID (e.g., NX-7K4P92).
 *
 * Privacy Guarantees:
 * - Mobile numbers and emails are never searchable or exposed.
 * - Firebase UID remains strictly internal.
 */
@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onSendInvite: (String) -> Unit
) {
    var studentIdInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("add_friend_dialog"),
        containerColor = NexoraSurfaceVariant,
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
                            .size(34.dp)
                            .background(NexoraCyan.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = null,
                            tint = NexoraCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "Add Friend by Student ID",
                        color = NexoraTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = NexoraTextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Connect with Gujarat Board Class 12 Commerce classmates using their permanent NEXORA Student ID to study together.",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                // ID input
                OutlinedTextField(
                    value = studentIdInput,
                    onValueChange = { input ->
                        studentIdInput = input.uppercase().trim()
                        errorMessage = null
                        successMessage = null
                    },
                    label = { Text("Enter Student ID") },
                    placeholder = { Text("e.g. NX-7K4P92") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_friend_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexoraCyan,
                        unfocusedBorderColor = NexoraBorder,
                        focusedContainerColor = NexoraSurface,
                        unfocusedContainerColor = NexoraSurface,
                        focusedTextColor = NexoraTextPrimary,
                        unfocusedTextColor = NexoraTextPrimary
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Badge,
                            contentDescription = null,
                            tint = NexoraCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                if (successMessage != null) {
                    Text(
                        text = successMessage!!,
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Instructions Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NexoraSurfaceElevated, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "How to find a Student ID:",
                            color = NexoraTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ask your classmate to open their Profile screen in NEXORA LEARN and tap 'Copy' on their 'My Student ID' card.",
                            color = NexoraTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Privacy Reassurance
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NexoraSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, NexoraBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = NexoraTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Phone numbers & emails are never searched or shared. Student IDs protect your privacy.",
                        color = NexoraTextMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val raw = studentIdInput.trim()
                    if (raw.isBlank()) {
                        errorMessage = "Please enter a Student ID (e.g. NX-7K4P92)."
                        return@Button
                    }

                    // Check if user accidentally typed phone number or email
                    if (raw.contains("@") || raw.all { it.isDigit() } && raw.length >= 10) {
                        errorMessage = "For student privacy, searching by email or phone number is not allowed. Please enter their unique Student ID (e.g. NX-7K4P92)."
                        return@Button
                    }

                    val normalized = StudentIdGenerator.normalizeStudentId(raw)
                    if (normalized == null && !raw.startsWith("NX-", ignoreCase = true) && raw.length < 4) {
                        errorMessage = "Invalid Student ID format. Expected format is NX-XXXXXX (e.g. NX-7K4P92)."
                        return@Button
                    }

                    val finalId = normalized ?: (if (!raw.startsWith("NX-", ignoreCase = true)) "NX-${raw.uppercase()}" else raw.uppercase())
                    onSendInvite(finalId)
                    successMessage = "Friend invite sent for $finalId!"
                    studentIdInput = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexoraCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("send_invite_btn")
            ) {
                Text(
                    text = "Send Request",
                    color = NexoraBackground,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    )
}
