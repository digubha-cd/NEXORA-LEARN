package com.example.ui.screens.friends

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.ChatMessage
import com.example.core.model.Friend
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraSuccess
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary
import kotlinx.coroutines.delay

/**
 * Full-Screen 1-to-1 Chat Page with Message Reactions & Typing Indicator.
 *
 * Requirements:
 * - Full-screen 1-to-1 Chat page
 * - Proper large message area
 * - Fixed message input pinned at bottom
 * - Timestamps & read/unread status
 * - Message reactions (emojis: 👍, ❤️, 🔥, 👏, 🎯) with quick picker and count bubbles
 * - 'typing...' indicator with animated pulsing dots for communication feedback
 * - Strict Privacy & Security: No mobile numbers or Gmail exposed
 */
@Composable
fun FriendChatScreen(
    friend: Friend,
    messages: List<ChatMessage>,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onToggleReaction: (messageId: String, emoji: String) -> Unit = { _, _ -> },
    onOpenFriendProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var isFriendTyping by remember { mutableStateOf(false) }

    // Simulate smart interactive peer feedback:
    // When the current student sends a message and the friend is online,
    // trigger a short, private 'typing...' indicator before the friend acknowledges
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
            val lastMsg = messages.lastOrNull()
            if (lastMsg != null && lastMsg.isFromMe && friend.isOnline) {
                delay(700)
                isFriendTyping = true
                delay(2200)
                isFriendTyping = false
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("chat_full_screen"),
        containerColor = NexoraBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NexoraSurface)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("chat_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NexoraTextPrimary
                            )
                        }

                        // Friend avatar & info (tap to view profile)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenFriendProfile)
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(friend.avatarColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = friend.avatarInitials,
                                    color = Color.White,
                                    fontSize = 15.sp,
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
                                    if (isFriendTyping) {
                                        Text(
                                            text = "typing...",
                                            color = NexoraCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else if (friend.isOnline) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .background(NexoraSuccess, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Online",
                                            color = NexoraSuccess,
                                            fontSize = 11.sp
                                        )
                                    } else {
                                        Text(
                                            text = "Study Buddy • Class 12 Commerce",
                                            color = NexoraTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Profile icon button
                    IconButton(
                        onClick = onOpenFriendProfile,
                        modifier = Modifier
                            .size(36.dp)
                            .background(NexoraSurfaceElevated, CircleShape)
                            .testTag("chat_profile_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "View Profile",
                            tint = NexoraCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Fixed Message Input Area at Bottom with Navigation Insets
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NexoraSurface)
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Message ${friend.name}...",
                                color = NexoraTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexoraCyan,
                            unfocusedBorderColor = NexoraBorder,
                            focusedContainerColor = NexoraSurfaceElevated,
                            unfocusedContainerColor = NexoraSurfaceElevated,
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
                            .size(48.dp)
                            .background(NexoraCyan, CircleShape)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = NexoraBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Privacy Assurance Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NexoraSurfaceElevated, RoundedCornerShape(10.dp))
                    .border(1.dp, NexoraBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = NexoraTextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "End-to-end private chat • Phone numbers & Gmail are never shared",
                    color = NexoraTextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Large Messages Area
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(friend.avatarColorHex).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = friend.avatarInitials,
                                color = Color(friend.avatarColorHex),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Start studying together with ${friend.name}!",
                            color = NexoraTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Ask questions, share study targets, or discuss Accounts & Stats chapters.",
                            color = NexoraTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatBubbleWithReactions(
                            message = msg,
                            onToggleReaction = { emoji ->
                                onToggleReaction(msg.id, emoji)
                            }
                        )
                    }

                    // Typing Indicator Bubble in message list
                    if (isFriendTyping) {
                        item {
                            TypingIndicatorBubble(friend = friend)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Chat bubble supporting message reactions (emojis).
 * Tapping the reaction button shows a subtle horizontal emoji picker.
 */
@Composable
private fun ChatBubbleWithReactions(
    message: ChatMessage,
    onToggleReaction: (String) -> Unit
) {
    val isMe = message.isFromMe
    var showReactionPicker by remember { mutableStateOf(false) }
    val quickEmojis = listOf("👍", "❤️", "🔥", "👏", "🎯")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // Optional quick emoji picker bar above bubble
        AnimatedVisibility(
            visible = showReactionPicker,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(NexoraSurfaceVariant)
                    .border(1.dp, NexoraBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                quickEmojis.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                onToggleReaction(emoji)
                                showReactionPicker = false
                            }
                            .padding(4.dp)
                            .testTag("reaction_${emoji}_btn")
                    )
                }
            }
        }

        if (showReactionPicker) {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Message body with tap-to-toggle reaction picker
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isMe) {
                // Reaction trigger on left for my messages
                IconButton(
                    onClick = { showReactionPicker = !showReactionPicker },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("react_msg_${message.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddReaction,
                        contentDescription = "Add Reaction",
                        tint = NexoraTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        )
                    )
                    .background(if (isMe) NexoraCyan else NexoraSurfaceElevated)
                    .border(
                        1.dp,
                        if (isMe) NexoraCyan else NexoraBorder,
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        )
                    )
                    .clickable { showReactionPicker = !showReactionPicker }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (isMe) NexoraBackground else NexoraTextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 19.sp
                )
            }

            if (!isMe) {
                Spacer(modifier = Modifier.width(4.dp))
                // Reaction trigger on right for friend messages
                IconButton(
                    onClick = { showReactionPicker = !showReactionPicker },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("react_msg_${message.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddReaction,
                        contentDescription = "Add Reaction",
                        tint = NexoraTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Active Reactions Pill Row
        if (message.reactions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                message.reactions.forEach { (emoji, count) ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NexoraSurfaceVariant)
                            .border(1.dp, NexoraBorder, RoundedCornerShape(12.dp))
                            .clickable { onToggleReaction(emoji) }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(text = emoji, fontSize = 12.sp)
                        if (count > 1) {
                            Text(
                                text = "$count",
                                color = NexoraTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Timestamp & Read Indicators
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = message.formattedTime,
                color = NexoraTextMuted,
                fontSize = 10.sp
            )
            if (isMe) {
                Icon(
                    imageVector = if (message.isRead) Icons.Outlined.DoneAll else Icons.Outlined.Check,
                    contentDescription = if (message.isRead) "Read" else "Sent",
                    tint = if (message.isRead) NexoraCyan else NexoraTextMuted,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

/**
 * Animated 'typing...' indicator bubble with three smooth pulsing dots.
 */
@Composable
private fun TypingIndicatorBubble(friend: Friend) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_dots")

    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, delayMillis = 150),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, delayMillis = 300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.testTag("typing_indicator_bubble")
    ) {
        // Friend avatar small
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(friend.avatarColorHex)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = friend.avatarInitials,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Pill with 3 animated dots
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(NexoraSurfaceElevated)
                .border(1.dp, NexoraBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${friend.name.split(" ").firstOrNull() ?: friend.name} is typing",
                    color = NexoraTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .offset(y = dot1Offset.dp)
                        .size(5.dp)
                        .background(NexoraCyan, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot2Offset.dp)
                        .size(5.dp)
                        .background(NexoraCyan, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot3Offset.dp)
                        .size(5.dp)
                        .background(NexoraCyan, CircleShape)
                )
            }
        }
    }
}
