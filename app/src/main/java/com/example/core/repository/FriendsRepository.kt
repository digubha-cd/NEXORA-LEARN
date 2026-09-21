package com.example.core.repository

import com.example.core.model.ChatMessage
import com.example.core.model.Friend
import com.example.core.model.FriendshipStatus
import com.example.core.model.LeaderboardEntry
import com.example.core.model.StudentProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

/**
 * FriendsRepository
 *
 * Manages:
 * 1. Friends list (accepted, incoming requests, outgoing invitations)
 * 2. Add / Invite friend by student code or name
 * 3. 1-to-1 private chat messages with timestamps, read/unread states, new message indicators
 * 4. Friends Study Competition & Leaderboard calculated from objective study activity:
 *    - Completed student-created To-Do tasks (10 pts each)
 *    - Current study streak (15 pts per day)
 *    - Top 10 rankings display with 🥇 1st, 🥈 2nd, 🥉 3rd, 4th–10th
 *    - Current student rank ("Your Rank: #XX") always prominently displayed even outside top 10
 *
 * Privacy:
 * - Mobile numbers, emails, and credentials are strictly omitted from Friend and Chat representations.
 */
object FriendsRepository {

    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    // 1-to-1 Chat messages store mapped by friendId -> List<ChatMessage>
    private val _chatMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val chatMessages: StateFlow<Map<String, List<ChatMessage>>> = _chatMessages.asStateFlow()

    /**
     * Total unread messages across all friends
     */
    fun getTotalUnreadMessageCount(): Int {
        return _friends.value.filter { it.friendshipStatus == FriendshipStatus.ACCEPTED }
            .sumOf { it.unreadMessageCount }
    }

    /**
     * Total pending friend requests count
     */
    fun getPendingRequestsCount(): Int {
        return _friends.value.count { it.friendshipStatus == FriendshipStatus.PENDING_INCOMING }
    }

    /**
     * Total notification badge count (unread messages + pending friend requests)
     */
    fun getTotalNotificationCount(): Int {
        return getTotalUnreadMessageCount() + getPendingRequestsCount()
    }

    /**
     * Get accepted friends list
     */
    fun getAcceptedFriends(): List<Friend> {
        return _friends.value.filter { it.friendshipStatus == FriendshipStatus.ACCEPTED }
    }

    /**
     * Get incoming friend requests
     */
    fun getIncomingRequests(): List<Friend> {
        return _friends.value.filter { it.friendshipStatus == FriendshipStatus.PENDING_INCOMING }
    }

    /**
     * Accept a pending friend request
     */
    fun acceptFriendRequest(friendId: String) {
        _friends.value = _friends.value.map {
            if (it.id == friendId) it.copy(friendshipStatus = FriendshipStatus.ACCEPTED) else it
        }
    }

    /**
     * Reject or remove friend
     */
    fun removeFriend(friendId: String) {
        _friends.value = _friends.value.filterNot { it.id == friendId }
    }

    /**
     * Send friend invite by student name or code
     */
    fun sendFriendInvite(name: String): Boolean {
        if (name.isBlank()) return false
        val newFriend = Friend(
            id = "invite_${UUID.randomUUID()}",
            name = name.trim(),
            avatarInitials = name.trim().take(2).uppercase(),
            avatarColorHex = 0xFF00E5FF,
            targetMarks = "90+ Marks",
            completedTodoCount = 0,
            studyStreakDays = 0,
            competitionPoints = 0,
            leaderboardRank = 0,
            isOnline = false,
            statusText = "Invite Sent",
            friendshipStatus = FriendshipStatus.PENDING_OUTGOING
        )
        _friends.value = _friends.value + newFriend
        return true
    }

    /**
     * Send a 1-to-1 chat message to a friend
     */
    fun sendMessage(friendId: String, text: String): Boolean {
        if (text.isBlank()) return false
        val now = System.currentTimeMillis()
        val timeStr = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).format(TIME_FORMATTER)

        val newMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            friendId = friendId,
            text = text.trim(),
            isFromMe = true,
            timestamp = now,
            formattedTime = timeStr,
            isRead = true
        )

        val currentList = _chatMessages.value[friendId].orEmpty()
        _chatMessages.value = _chatMessages.value + (friendId to (currentList + newMessage))
        return true
    }

    /**
     * Toggle or add an emoji reaction on a message
     */
    fun toggleReaction(friendId: String, messageId: String, emoji: String) {
        val currentList = _chatMessages.value[friendId] ?: return
        val updated = currentList.map { msg ->
            if (msg.id == messageId) {
                val currentCount = msg.reactions[emoji] ?: 0
                val newReactions = if (currentCount > 0) {
                    msg.reactions - emoji
                } else {
                    msg.reactions + (emoji to 1)
                }
                msg.copy(reactions = newReactions)
            } else {
                msg
            }
        }
        _chatMessages.value = _chatMessages.value + (friendId to updated)
    }

    /**
     * Mark messages with a friend as read
     */
    fun markChatAsRead(friendId: String) {
        val currentList = _chatMessages.value[friendId]
        if (currentList != null) {
            val updated = currentList.map { it.copy(isRead = true) }
            _chatMessages.value = _chatMessages.value + (friendId to updated)
        }
        _friends.value = _friends.value.map {
            if (it.id == friendId) it.copy(unreadMessageCount = 0) else it
        }
    }

    /**
     * Get 1-to-1 chat messages for a specific friend
     */
    fun getChatMessages(friendId: String): List<ChatMessage> {
        return _chatMessages.value[friendId].orEmpty()
    }

    /**
     * Generate dynamic leaderboard combining accepted friends and the current student.
     * Objective scoring:
     * - Each completed student To-Do: 10 points
     * - Each streak day: 15 points
     */
    fun getLeaderboard(
        currentStudentProfile: StudentProfile,
        studentCompletedTodos: Int,
        studentStreakDays: Int,
        studentCompletedChapterIds: Set<String> = SyllabusRepository.completedChapterIds.value
    ): Pair<List<LeaderboardEntry>, LeaderboardEntry> {
        val studentPoints = (studentCompletedTodos * 10) + (studentStreakDays * 15)

        val currentStudentEntry = LeaderboardEntry(
            rank = 0,
            studentId = currentStudentProfile.userId.ifBlank { "me" },
            studentName = currentStudentProfile.studentName.ifBlank { "You" },
            avatarInitials = (currentStudentProfile.studentName.ifBlank { "ME" }).take(2).uppercase(),
            avatarColorHex = 0xFF00E5FF,
            completedTodoCount = studentCompletedTodos,
            streakDays = studentStreakDays,
            competitionPoints = studentPoints,
            isCurrentStudent = true,
            targetMarks = currentStudentProfile.targetMarks,
            completedChapterIds = studentCompletedChapterIds
        )

        val peerEntries = getAcceptedFriends().map { friend ->
            LeaderboardEntry(
                rank = 0,
                studentId = friend.id,
                studentName = friend.name,
                avatarInitials = friend.avatarInitials,
                avatarColorHex = friend.avatarColorHex,
                completedTodoCount = friend.completedTodoCount,
                streakDays = friend.studyStreakDays,
                competitionPoints = friend.competitionPoints,
                isCurrentStudent = false,
                targetMarks = friend.targetMarks,
                completedChapterIds = friend.completedChapterIds
            )
        }

        // Leaderboard contains ONLY real authenticated peers + current student
        val allParticipants = (listOf(currentStudentEntry) + peerEntries)
            .distinctBy { it.studentId }
            .sortedByDescending { it.competitionPoints }

        // Assign rankings
        val rankedList = allParticipants.mapIndexed { index, item ->
            item.copy(rank = index + 1)
        }

        val myRankedEntry = rankedList.firstOrNull { it.isCurrentStudent } ?: currentStudentEntry.copy(rank = rankedList.size + 1)
        val top10 = rankedList.take(10)

        return Pair(top10, myRankedEntry)
    }

    /**
     * Reset all friends & chat state (for test cleanup).
     */
    fun resetAllData() {
        _friends.value = emptyList()
        _chatMessages.value = emptyMap()
    }
}
