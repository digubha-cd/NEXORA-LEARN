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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.ChatMessage
import com.example.core.model.Friend
import com.example.core.repository.FriendsRepository
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary
import kotlinx.coroutines.launch

/**
 * 1-to-1 Private Chat Dialog between accepted study friends (Step 8A).
 * Features:
 * - Send text messages
 * - Message timestamps
 * - Read / unread status
 * - New message indicator
 * - Safe privacy: No phone numbers or emails shown
 */
@Composable
fun FriendChatDialog(
    friend: Friend,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to bottom whenever new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("friend_chat_dialog"),
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(friend.avatarColorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = friend.avatarInitials,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = friend.name,
                            color = NexoraTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (friend.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF10B981), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Online",
                                    color = Color(0xFF10B981),
                                    fontSize = 10.sp
                                )
                            } else {
                                Text(
                                    text = "Study Buddy",
                                    color = NexoraTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close Chat",
                        tint = NexoraTextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 380.dp)
            ) {
                // Privacy Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NexoraSurfaceElevated, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = NexoraTextMuted,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "End-to-end private chat • No phone/email shared",
                        color = NexoraTextMuted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Messages list
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Say hi to ${friend.name}!\nDiscuss study tasks, Accounts & Stats.",
                            color = NexoraTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatBubble(message = msg)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input field + Send button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Message ${friend.name}...",
                                color = NexoraTextMuted,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexoraCyan,
                            unfocusedBorderColor = NexoraBorder,
                            focusedContainerColor = NexoraSurface,
                            unfocusedContainerColor = NexoraSurface,
                            focusedTextColor = NexoraTextPrimary,
                            unfocusedTextColor = NexoraTextPrimary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    onSendMessage(inputText)
                                    inputText = ""
                                }
                            }
                        )
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(NexoraCyan, RoundedCornerShape(12.dp))
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = NexoraBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isMe = message.isFromMe

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isMe) 12.dp else 2.dp,
                        bottomEnd = if (isMe) 2.dp else 12.dp
                    )
                )
                .background(if (isMe) NexoraCyan else NexoraSurfaceElevated)
                .border(
                    1.dp,
                    if (isMe) NexoraCyan else NexoraBorder,
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isMe) 12.dp else 2.dp,
                        bottomEnd = if (isMe) 2.dp else 12.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.text,
                color = if (isMe) NexoraBackground else NexoraTextPrimary,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = message.formattedTime,
                color = NexoraTextMuted,
                fontSize = 9.sp
            )
            if (isMe) {
                Icon(
                    imageVector = if (message.isRead) Icons.Outlined.DoneAll else Icons.Outlined.Check,
                    contentDescription = if (message.isRead) "Read" else "Sent",
                    tint = if (message.isRead) NexoraCyan else NexoraTextMuted,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}
