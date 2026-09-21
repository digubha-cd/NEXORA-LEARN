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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary

/**
 * Add / Invite Friend Dialog (Step 8A).
 * Lets students invite classmates by name or student study code.
 * Safe privacy: Mobile numbers and emails are never exposed.
 */
@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onSendInvite: (String) -> Unit
) {
    var friendInput by remember { mutableStateOf("") }
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
                            .background(NexoraCyan.copy(alpha = 0.2f), CircleShape),
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
                        text = "Add Study Friend",
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
                    text = "Connect with Gujarat Board Class 12 Commerce classmates to share study motivation and compete for 90+ marks.",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                OutlinedTextField(
                    value = friendInput,
                    onValueChange = {
                        friendInput = it
                        errorMessage = null
                    },
                    label = { Text("Student Name or Study Code") },
                    placeholder = { Text("e.g. Nirav Shah or NX-9012") },
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
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = androidx.compose.ui.graphics.Color(0xFFEF4444),
                        fontSize = 11.sp
                    )
                }

                if (successMessage != null) {
                    Text(
                        text = successMessage!!,
                        color = androidx.compose.ui.graphics.Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Privacy Note
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NexoraSurfaceElevated, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = NexoraTextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Safe & Private: Classmates only see your study activity & streak. Phone/email are never shared.",
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
                    if (friendInput.isBlank()) {
                        errorMessage = "Please enter a student name or code"
                    } else {
                        onSendInvite(friendInput.trim())
                        successMessage = "Study invite sent to $friendInput!"
                        friendInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexoraCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("send_invite_btn")
            ) {
                Text(
                    text = "Send Invite",
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
