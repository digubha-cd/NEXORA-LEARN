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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Stars
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.LeaderboardEntry
import com.example.ui.components.NexoraTargetBadge
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraElectricBlue
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraPink
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary

/**
 * Friends Study Competition & Leaderboard Dialog (Step 8A).
 *
 * Requirements:
 * - Scored on objective study activity:
 *   - Completed student-created To-Do tasks
 *   - Study streak
 *   - Consistent study activity
 * - Leaderboard displaying:
 *   🥇 1st
 *   🥈 2nd
 *   🥉 3rd
 *   4th–10th
 * - Show only Top 10 rankings.
 * - Always show the current student's own rank prominently ("Your Rank: #XX")
 *   even if outside the Top 10, without needing to scroll through a long list.
 * - Tapping any student lets the user inspect their public study profile.
 */
@Composable
fun FriendsLeaderboardDialog(
    top10Entries: List<LeaderboardEntry>,
    myEntry: LeaderboardEntry,
    onDismiss: () -> Unit,
    onStudentClick: (LeaderboardEntry) -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("friends_leaderboard_dialog"),
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
                            .background(NexoraGold.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = NexoraGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Study Competition",
                            color = NexoraTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Class 12 Commerce Leaderboard",
                            color = NexoraTextMuted,
                            fontSize = 10.sp
                        )
                    }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
            ) {
                // FIXED PINNED BANNER: Current Student's Rank ("Your Rank: #XX")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NexoraCyan.copy(alpha = 0.12f))
                        .border(1.5.dp, NexoraCyan, RoundedCornerShape(12.dp))
                        .clickable {
                            onDismiss()
                            onStudentClick(myEntry)
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("my_pinned_rank_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NexoraCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = myEntry.avatarInitials,
                                    color = NexoraBackground,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${myEntry.studentName} (You)",
                                        color = NexoraTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${myEntry.completedTodoCount} To-Dos",
                                        color = NexoraCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(text = "•", color = NexoraTextMuted, fontSize = 10.sp)
                                    Text(
                                        text = "${myEntry.streakDays}d Streak",
                                        color = NexoraGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Prominent Rank & Score
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Your Rank: #${myEntry.rank}",
                                color = NexoraCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${myEntry.competitionPoints} Pts",
                                color = NexoraGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scoring explanation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Top 10 Rankings",
                        color = NexoraTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap any student to view profile",
                        color = NexoraCyan,
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Top 10 Leaderboard List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(top10Entries, key = { "${it.studentId}_${it.rank}" }) { entry ->
                        LeaderboardRowItem(
                            entry = entry,
                            onClick = {
                                onDismiss()
                                onStudentClick(entry)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = NexoraCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun LeaderboardRowItem(
    entry: LeaderboardEntry,
    onClick: () -> Unit = {}
) {
    val isTop3 = entry.rank in 1..3
    val isMe = entry.isCurrentStudent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isMe -> NexoraCyan.copy(alpha = 0.12f)
                    isTop3 -> NexoraGold.copy(alpha = 0.06f)
                    else -> NexoraSurfaceElevated
                }
            )
            .border(
                1.dp,
                when {
                    isMe -> NexoraCyan
                    isTop3 -> NexoraGold.copy(alpha = 0.35f)
                    else -> NexoraBorder
                },
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("leaderboard_row_${entry.rank}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Rank badge + Avatar + Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Rank Badge: 🥇, 🥈, 🥉 or #4–#10
            Box(
                modifier = Modifier.width(36.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = entry.rankBadge,
                    color = if (isTop3) NexoraGold else NexoraTextSecondary,
                    fontSize = if (isTop3) 14.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(entry.avatarColorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.avatarInitials,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = if (isMe) "${entry.studentName} (You)" else entry.studentName,
                    color = if (isMe) NexoraCyan else NexoraTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = if (isMe || isTop3) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${entry.completedTodoCount} To-Dos",
                        color = NexoraTextSecondary,
                        fontSize = 9.sp
                    )
                    Text(text = "•", color = NexoraTextMuted, fontSize = 9.sp)
                    Text(
                        text = "${entry.streakDays}d Streak",
                        color = NexoraGold,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Right: Points
        Text(
            text = "${entry.competitionPoints} Pts",
            color = if (isTop3) NexoraGold else NexoraTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
