package com.example.ui.screens.friends

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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Friend
import com.example.core.model.FriendshipStatus
import com.example.core.repository.FriendsRepository
import com.example.ui.components.NexoraCard
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraPink
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraSuccess
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary

/**
 * Full-Screen Friends Page (Step 8A).
 * Requirements:
 * - Full-screen Friends page
 * - Friends list
 * - Online/status indicators
 * - Add/Invite Friend
 * - Friend requests (accept / decline)
 * - Unread message/request badge
 * - Tap a friend -> opens full Friend Profile page
 * - Back button to return to Home
 */
@Composable
fun FriendsScreen(
    friends: List<Friend>,
    totalNotificationCount: Int,
    onBackClick: () -> Unit,
    onFriendClick: (Friend) -> Unit,
    onOpenChat: (Friend) -> Unit,
    onOpenCompetition: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val acceptedFriends = remember(friends) {
        friends.filter { it.friendshipStatus == FriendshipStatus.ACCEPTED }
    }
    val filteredFriends = remember(acceptedFriends, searchQuery) {
        if (searchQuery.isBlank()) {
            acceptedFriends
        } else {
            val query = searchQuery.trim().lowercase()
            acceptedFriends.filter { it.name.lowercase().contains(query) }
        }
    }
    val incomingRequests = remember(friends) {
        friends.filter { it.friendshipStatus == FriendshipStatus.PENDING_INCOMING }
    }
    val outgoingRequests = remember(friends) {
        friends.filter { it.friendshipStatus == FriendshipStatus.PENDING_OUTGOING }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("friends_full_screen"),
        containerColor = NexoraBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NexoraSurface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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
                                .testTag("friends_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NexoraTextPrimary
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Study Friends",
                                    color = NexoraTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (totalNotificationCount > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(NexoraPink, CircleShape)
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$totalNotificationCount New",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${acceptedFriends.size} Active Study Buddies • Class 12 Commerce",
                                color = NexoraTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Top Actions: Competition & Add
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onOpenCompetition,
                            modifier = Modifier
                                .size(36.dp)
                                .background(NexoraGold.copy(alpha = 0.15f), CircleShape)
                                .testTag("friends_page_competition_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = "Competition Leaderboard",
                                tint = NexoraGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .background(NexoraCyan.copy(alpha = 0.15f), CircleShape)
                                .testTag("friends_page_add_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PersonAdd,
                                contentDescription = "Add Friend",
                                tint = NexoraCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar for quickly filtering friends by name
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("friends_search_bar"),
                    placeholder = {
                        Text(
                            text = "Search friends by name...",
                            color = NexoraTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = if (searchQuery.isNotEmpty()) NexoraCyan else NexoraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.testTag("friends_search_clear_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear search",
                                    tint = NexoraTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NexoraTextPrimary,
                        unfocusedTextColor = NexoraTextPrimary,
                        focusedContainerColor = NexoraSurfaceElevated,
                        unfocusedContainerColor = NexoraSurfaceElevated,
                        focusedBorderColor = NexoraCyan,
                        unfocusedBorderColor = NexoraBorder,
                        cursorColor = NexoraCyan
                    )
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Safe Privacy Guarantee Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NexoraSurfaceElevated, RoundedCornerShape(12.dp))
                        .border(1.dp, NexoraBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = NexoraCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Safe & Private: Phone numbers, Gmail, and account details are strictly confidential. Friends only see study streak & To-Do progress.",
                        color = NexoraTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            // Quick Banner to open Competition
            item {
                NexoraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenCompetition)
                        .testTag("friends_competition_banner"),
                    containerColor = NexoraSurface,
                    borderColor = NexoraGold.copy(alpha = 0.4f),
                    contentPadding = 14.dp
                ) {
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
                                    .size(40.dp)
                                    .background(NexoraGold.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = null,
                                    tint = NexoraGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Class 12 Study Competition",
                                    color = NexoraTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Scored by completed To-Dos + Daily Streaks",
                                    color = NexoraTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = "Leaderboard →",
                            color = NexoraGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Pending Incoming Friend Requests
            if (incomingRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "Friend Requests (${incomingRequests.size})",
                        color = NexoraTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(incomingRequests, key = { "req_${it.id}" }) { req ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NexoraPurple.copy(alpha = 0.12f))
                            .border(1.dp, NexoraPurple.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(req.avatarColorHex), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = req.avatarInitials,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(
                                    text = req.name,
                                    color = NexoraTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Gujarat Board Commerce Student",
                                    color = NexoraTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { FriendsRepository.acceptFriendRequest(req.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = NexoraCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "Accept",
                                    color = NexoraBackground,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { FriendsRepository.removeFriend(req.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Decline",
                                    tint = NexoraTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Accepted Friends Header & List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "Search Results (${filteredFriends.size})"
                        } else {
                            "All Study Buddies (${acceptedFriends.size})"
                        },
                        color = NexoraTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (searchQuery.isNotBlank()) {
                        Text(
                            text = "Filtered by \"$searchQuery\"",
                            color = NexoraCyan,
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = "Tap to view profile",
                            color = NexoraTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (acceptedFriends.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NexoraSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, NexoraBorder, RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Group,
                                contentDescription = null,
                                tint = NexoraTextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Study Friends Yet",
                                color = NexoraTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Invite your Class 12 Commerce classmates to share daily study goals and compete for 90+ marks!",
                                color = NexoraTextMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NexoraCyan),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PersonAdd,
                                    contentDescription = null,
                                    tint = NexoraBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Add Friend",
                                    color = NexoraBackground,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else if (filteredFriends.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NexoraSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, NexoraBorder, RoundedCornerShape(12.dp))
                            .padding(24.dp)
                            .testTag("friends_search_empty_state"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = NexoraTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Friends Found",
                                color = NexoraTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No study friends matching \"$searchQuery\"",
                                color = NexoraTextMuted,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.testTag("friends_clear_filter_btn")
                            ) {
                                Text(
                                    text = "Clear Filter",
                                    color = NexoraCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredFriends, key = { it.id }) { friend ->
                    FullScreenFriendItem(
                        friend = friend,
                        onFriendClick = { onFriendClick(friend) },
                        onChatClick = { onOpenChat(friend) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAddDialog) {
        AddFriendDialog(
            onDismiss = { showAddDialog = false },
            onSendInvite = { name ->
                FriendsRepository.sendFriendInvite(name)
            }
        )
    }
}

@Composable
private fun FullScreenFriendItem(
    friend: Friend,
    onFriendClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NexoraSurface)
            .border(1.dp, NexoraBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onFriendClick)
            .padding(14.dp)
            .testTag("friend_item_${friend.id}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Avatar + Online status indicator
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
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

                if (friend.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(NexoraSuccess, CircleShape)
                            .border(2.dp, NexoraSurface, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = friend.name,
                        color = NexoraTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Rank #${friend.leaderboardRank}",
                        color = NexoraGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Whatshot,
                            contentDescription = null,
                            tint = NexoraGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${friend.studyStreakDays}d streak",
                            color = NexoraTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Text(text = "•", color = NexoraTextMuted, fontSize = 10.sp)

                    Text(
                        text = "${friend.completedTodoCount} To-Dos done",
                        color = NexoraCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Chat Action Button with badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (friend.unreadMessageCount > 0) NexoraCyan.copy(alpha = 0.2f) else NexoraSurfaceElevated)
                .border(
                    1.dp,
                    if (friend.unreadMessageCount > 0) NexoraCyan else NexoraBorder,
                    RoundedCornerShape(10.dp)
                )
                .clickable(onClick = onChatClick)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("friend_chat_btn_${friend.id}")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Chat with ${friend.name}",
                    tint = if (friend.unreadMessageCount > 0) NexoraCyan else NexoraTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                if (friend.unreadMessageCount > 0) {
                    Box(
                        modifier = Modifier
                            .background(NexoraCyan, CircleShape)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${friend.unreadMessageCount}",
                            color = NexoraBackground,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Chat",
                        color = NexoraTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
