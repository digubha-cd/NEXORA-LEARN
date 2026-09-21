package com.example.ui.screens.friends

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.LeaderboardEntry
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraSurfaceVariant
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary

// Podium Accent Tones
private val GoldAccent = Color(0xFFFFD700)
private val SilverAccent = Color(0xFFE0E6ED)
private val BronzeAccent = Color(0xFFCD7F32)

/**
 * Full-Screen Competition & Leaderboard Page.
 *
 * Requirements:
 * - "Leaderboard" title at top
 * - "Top 10 Students of your class" subtitle
 * - Large podium section for 1st, 2nd, and 3rd with distinct heights, avatars, badges, and points
 * - Student avatar + name + points
 * - Ranked list for 4th–10th below
 * - Clear rank number on the right
 * - Competition points clearly visible
 * - Tapping any student (including self, podium, or list item) opens their complete PUBLIC study profile
 * - Clean full-screen mobile layout with smooth scrolling and generous spacing
 * - Fixed/floating "Your Rank: #XX" banner when student is outside Top 10 so they don't have to scroll
 * - Strict adherence to NEXORA LEARN design system: dark palette, soft glowing borders, clean typography
 * - Scoring and privacy rules remain unchanged and clearly communicated
 */
@Composable
fun CompetitionScreen(
    top10Entries: List<LeaderboardEntry>,
    myEntry: LeaderboardEntry,
    onBackClick: () -> Unit,
    onStudentClick: (LeaderboardEntry) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val firstEntry = top10Entries.find { it.rank == 1 }
    val secondEntry = top10Entries.find { it.rank == 2 }
    val thirdEntry = top10Entries.find { it.rank == 3 }
    val remainingEntries = top10Entries.filter { it.rank in 4..10 }
    val isOutsideTop10 = myEntry.rank > 10

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("competition_full_screen"),
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
                                .testTag("competition_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NexoraTextPrimary
                            )
                        }

                        Column {
                            Text(
                                text = "Leaderboard",
                                color = NexoraTextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Top 10 Students of your class",
                                color = NexoraCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NexoraGold.copy(alpha = 0.15f))
                            .border(1.dp, NexoraGold.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = NexoraGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            // FIXED PROMINENT BAR IF OUTSIDE TOP 10:
            // Ensures "Your Rank: #XX" is immediately visible without scrolling
            if (isOutsideTop10) {
                Surface(
                    color = NexoraSurfaceElevated,
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .border(1.dp, NexoraCyan.copy(alpha = 0.5f))
                        .clickable { onStudentClick(myEntry) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("my_floating_rank_bar"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(NexoraCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = myEntry.avatarInitials,
                                    color = NexoraBackground,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(
                                    text = "${myEntry.studentName} (You)",
                                    color = NexoraTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${myEntry.completedTodoCount} To-Dos • ${myEntry.streakDays}d streak • Tap to view profile",
                                    color = NexoraTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Your Rank: #${myEntry.rank}",
                                color = NexoraCyan,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${myEntry.competitionPoints} Pts",
                                color = NexoraGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .then(if (!isOutsideTop10) Modifier.navigationBarsPadding() else Modifier),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // If user is inside top 10, pin their prominent card at top
                // If outside top 10, they also see this top overview card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NexoraCyan.copy(alpha = 0.10f))
                        .border(1.5.dp, NexoraCyan.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                        .clickable { onStudentClick(myEntry) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("my_pinned_rank_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(NexoraCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = myEntry.avatarInitials,
                                    color = NexoraBackground,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column {
                                Text(
                                    text = "${myEntry.studentName} (You)",
                                    color = NexoraTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${myEntry.completedTodoCount} To-Dos Done",
                                        color = NexoraCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(text = "•", color = NexoraTextMuted, fontSize = 11.sp)
                                    Text(
                                        text = "${myEntry.streakDays}d Streak",
                                        color = NexoraGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Prominent Rank & Points
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Your Rank: #${myEntry.rank}",
                                color = NexoraCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${myEntry.competitionPoints} Pts",
                                color = NexoraGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Scoring Transparency & Privacy Pill
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NexoraSurfaceElevated, RoundedCornerShape(12.dp))
                        .border(1.dp, NexoraBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Stars,
                            contentDescription = null,
                            tint = NexoraGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "To-Do = 10 pts",
                            color = NexoraTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Whatshot,
                            contentDescription = null,
                            tint = NexoraGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Day Streak = 15 pts",
                            color = NexoraTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = NexoraCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Private",
                            color = NexoraTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // LARGE PODIUM SECTION (1st, 2nd, 3rd)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    NexoraSurfaceVariant.copy(alpha = 0.85f),
                                    NexoraSurface.copy(alpha = 0.95f)
                                )
                            )
                        )
                        .border(1.dp, NexoraBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                        .testTag("leaderboard_podium_section"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = NexoraGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Top 3 Champions",
                                color = NexoraTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Tap student to view study profile",
                            color = NexoraCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Three Podium Columns: [2nd (Silver)] - [1st (Gold)] - [3rd (Bronze)]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 2nd Place (Silver)
                        PodiumColumn(
                            entry = secondEntry,
                            rank = 2,
                            rankLabel = "🥈 2nd",
                            accentColor = SilverAccent,
                            pedestalHeight = 90.dp,
                            avatarSize = 54.dp,
                            onStudentClick = onStudentClick,
                            modifier = Modifier.weight(1f)
                        )

                        // 1st Place (Gold - Tallest & Center)
                        PodiumColumn(
                            entry = firstEntry,
                            rank = 1,
                            rankLabel = "🥇 1st",
                            accentColor = GoldAccent,
                            pedestalHeight = 120.dp,
                            avatarSize = 64.dp,
                            onStudentClick = onStudentClick,
                            modifier = Modifier.weight(1.15f)
                        )

                        // 3rd Place (Bronze)
                        PodiumColumn(
                            entry = thirdEntry,
                            rank = 3,
                            rankLabel = "🥉 3rd",
                            accentColor = BronzeAccent,
                            pedestalHeight = 74.dp,
                            avatarSize = 50.dp,
                            onStudentClick = onStudentClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // RANKED LIST (4th - 10th) HEADER
            if (remainingEntries.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 2.dp, end = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ranked Students (4th – 10th)",
                            color = NexoraTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap student for full profile",
                            color = NexoraCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 4th to 10th List Items
                items(remainingEntries, key = { "${it.studentId}_${it.rank}" }) { entry ->
                    LeaderboardListItem(
                        entry = entry,
                        onClick = { onStudentClick(entry) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

/**
 * Single podium column representing 1st, 2nd, or 3rd place with
 * avatar, crown/badge, student name, points, and pedestal pillar.
 */
@Composable
private fun PodiumColumn(
    entry: LeaderboardEntry?,
    rank: Int,
    rankLabel: String,
    accentColor: Color,
    pedestalHeight: androidx.compose.ui.unit.Dp,
    avatarSize: androidx.compose.ui.unit.Dp,
    onStudentClick: (LeaderboardEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    if (entry == null) {
        Box(modifier = modifier)
        return
    }

    val isMe = entry.isCurrentStudent

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onStudentClick(entry) }
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Crown or Medallion icon on top of 1st place
        if (rank == 1) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = "Winner Crown",
                tint = GoldAccent,
                modifier = Modifier
                    .size(24.dp)
                    .offset(y = 4.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Avatar with glowing accent border
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(Color(entry.avatarColorHex))
                .border(2.5.dp, if (isMe) NexoraCyan else accentColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.avatarInitials,
                color = Color.White,
                fontSize = if (rank == 1) 18.sp else 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Student Name
        Text(
            text = if (isMe) "${entry.studentName} (You)" else entry.studentName,
            color = if (isMe) NexoraCyan else NexoraTextPrimary,
            fontSize = if (rank == 1) 13.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Points
        Text(
            text = "${entry.competitionPoints} pts",
            color = NexoraGold,
            fontSize = if (rank == 1) 12.sp else 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Podium Block Pillar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.28f),
                            NexoraSurfaceElevated.copy(alpha = 0.90f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = accentColor.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = rankLabel,
                    color = accentColor,
                    fontSize = if (rank == 1) 16.sp else 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${entry.completedTodoCount} done",
                    color = NexoraTextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Clean, modern ranked list item for 4th to 10th positions.
 * Left: Avatar + Student Name + To-Do count
 * Right: Rank number (#4-#10) & Competition Points
 */
@Composable
private fun LeaderboardListItem(
    entry: LeaderboardEntry,
    onClick: () -> Unit = {}
) {
    val isMe = entry.isCurrentStudent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isMe) NexoraCyan.copy(alpha = 0.12f)
                else NexoraSurface
            )
            .border(
                1.dp,
                if (isMe) NexoraCyan else NexoraBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("leaderboard_row_${entry.rank}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Avatar + Student Name + Stats
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(entry.avatarColorHex))
                    .border(
                        1.dp,
                        if (isMe) NexoraCyan else NexoraBorder,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.avatarInitials,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = if (isMe) "${entry.studentName} (You)" else entry.studentName,
                    color = if (isMe) NexoraCyan else NexoraTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (isMe) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${entry.completedTodoCount} To-Dos done",
                        color = NexoraTextSecondary,
                        fontSize = 11.sp
                    )
                    Text(text = "•", color = NexoraTextMuted, fontSize = 11.sp)
                    Text(
                        text = "${entry.streakDays}d Streak",
                        color = NexoraGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Right: Clear Rank number and Competition points
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = "#${entry.rank}",
                color = if (isMe) NexoraCyan else NexoraTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${entry.competitionPoints} Pts",
                color = NexoraGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
