package com.example.core.model

import java.util.UUID

/**
 * Friend representation in NEXORA LEARN.
 *
 * Privacy Guarantees:
 * - Only student name, avatar, 90+ target, subject-wise study progress, completed To-Do count,
 *   study streak, competition points, and leaderboard rank are visible.
 * - Mobile number, Gmail, login credentials, and account details are strictly private and NEVER exposed.
 */
data class Friend(
    val id: String = UUID.randomUUID().toString(),
    val studentId: String = "",
    val name: String,
    val avatarInitials: String = name.take(2).uppercase(),
    val avatarColorHex: Long = 0xFF00E5FF,
    val targetMarks: String = "90+ Marks",
    val completedTodoCount: Int = 0,
    val studyStreakDays: Int = 0,
    val competitionPoints: Int = 0,
    val leaderboardRank: Int = 0,
    val isOnline: Boolean = false,
    val statusText: String = "Class 12 Commerce",
    val unreadMessageCount: Int = 0,
    val subjectProgressMap: Map<String, Int> = emptyMap(), // subjectId -> percentage
    val completedChapterIds: Set<String> = emptySet(), // Real saved completed chapter IDs
    val friendshipStatus: FriendshipStatus = FriendshipStatus.ACCEPTED
)

enum class FriendshipStatus {
    ACCEPTED,
    PENDING_INCOMING,
    PENDING_OUTGOING
}

/**
 * Chat Message representation for 1-to-1 friend messaging.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val friendId: String,
    val text: String,
    val isFromMe: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String = "Just now",
    val isRead: Boolean = false,
    val reactions: Map<String, Int> = emptyMap() // emoji -> count, e.g. "👍" to 1, "🔥" to 2
)

/**
 * Friends Competition Leaderboard Entry
 */
data class LeaderboardEntry(
    val rank: Int,
    val studentId: String,
    val studentName: String,
    val avatarInitials: String,
    val avatarColorHex: Long,
    val completedTodoCount: Int,
    val streakDays: Int,
    val competitionPoints: Int,
    val isCurrentStudent: Boolean = false,
    val targetMarks: String = "90+ Marks",
    val completedChapterIds: Set<String> = emptySet()
) {
    val rankBadge: String
        get() = when (rank) {
            1 -> "🥇 1st"
            2 -> "🥈 2nd"
            3 -> "🥉 3rd"
            else -> "#$rank"
        }
}
