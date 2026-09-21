package com.example.ui.screens.friends

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.ui.components.NexoraCard
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraPink
import com.example.ui.theme.NexoraSuccess
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary

/**
 * Compact Friends Shortcut Card on Home Screen (Step 8A).
 * Requirements:
 * - "Keep only a compact Friends shortcut/card on Home."
 * - "Tapping it opens the full Friends page."
 * - Displays active study friends count, online avatars, and unread notification badge.
 */
@Composable
fun HomeFriendsSection(
    friends: List<Friend>,
    totalNotificationCount: Int,
    onOpenFriendsPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val acceptedFriends = remember(friends) {
        friends.filter { it.friendshipStatus == FriendshipStatus.ACCEPTED }
    }
    val onlineFriends = remember(acceptedFriends) {
        acceptedFriends.filter { it.isOnline }
    }

    NexoraCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenFriendsPage)
            .testTag("home_friends_card"),
        containerColor = NexoraSurface,
        borderColor = if (totalNotificationCount > 0) NexoraCyan.copy(alpha = 0.5f) else NexoraBorder,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Icon with glowing subtle border
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(NexoraCyan.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Group,
                        contentDescription = "Study Friends",
                        tint = NexoraCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Study Friends & Competition",
                            color = NexoraTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (totalNotificationCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(NexoraPink, CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "$totalNotificationCount New",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${acceptedFriends.size} Study Buddies",
                            color = NexoraTextSecondary,
                            fontSize = 11.sp
                        )
                        if (onlineFriends.isNotEmpty()) {
                            Text(text = "•", color = NexoraTextMuted, fontSize = 10.sp)
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(NexoraSuccess, CircleShape)
                            )
                            Text(
                                text = "${onlineFriends.size} online",
                                color = NexoraSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Right: Avatars preview + Arrow forward
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Overlapping avatar icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-8).dp)
                ) {
                    acceptedFriends.take(3).forEach { friend ->
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(friend.avatarColorHex))
                                .border(1.5.dp, NexoraSurface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = friend.avatarInitials,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open Friends",
                    tint = NexoraCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
