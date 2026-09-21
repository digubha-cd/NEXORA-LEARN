package com.example.core.repository

import android.util.Log
import com.example.core.model.ChatMessage
import com.example.core.model.Friend
import com.example.core.model.FriendshipStatus
import com.example.core.model.LeaderboardEntry
import com.example.core.model.StudentProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

/**
 * FriendsRepository
 *
 * Manages:
 * 1. Friends list (accepted, incoming requests, outgoing invitations) with real-time Firestore sync
 * 2. Add / Invite friend by student code or name with Firestore friend_requests collection
 * 3. 1-to-1 private chat messages with timestamps, read/unread states, new message indicators
 * 4. Friends Study Competition & Leaderboard calculated from objective study activity:
 *    - Completed student-created To-Do tasks (10 pts each)
 *    - Current study streak (15 pts per day)
 *    - Top 10 rankings display with 🥇 1st, 2nd, 3rd, 4th–10th
 *    - Current student rank ("Your Rank: #XX") always prominently displayed even outside top 10
 *
 * Privacy:
 * - Mobile numbers, emails, and credentials are strictly omitted from Friend and Chat representations.
 */
object FriendsRepository {

    private const val TAG = "FriendsRepository"
    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    // 1-to-1 Chat messages store mapped by friendId -> List<ChatMessage>
    private val _chatMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val chatMessages: StateFlow<Map<String, List<ChatMessage>>> = _chatMessages.asStateFlow()

    private var currentProfile: StudentProfile? = null

    // Firestore listener registrations
    private var incomingRequestsRegistration: ListenerRegistration? = null
    private var outgoingRequestsRegistration: ListenerRegistration? = null
    private var friendsListRegistration: ListenerRegistration? = null
    private val chatRegistrations = mutableMapOf<String, ListenerRegistration>()
    private val friendProfileRegistrations = mutableMapOf<String, ListenerRegistration>()

    private val isFirebaseAvailable: Boolean
        get() = try {
            FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()
        } catch (e: Throwable) {
            false
        }

    private val firestore: FirebaseFirestore?
        get() = if (isFirebaseAvailable) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Throwable) {
                null
            }
        } else null

    /**
     * Initialize repository with authenticated student profile and attach Firestore listeners.
     */
    fun initialize(profile: StudentProfile) {
        currentProfile = profile
        if (profile.userId.isBlank()) return

        stopListeners()

        val db = firestore ?: return
        val currentUid = profile.userId

        try {
            // 1. Listen for Incoming Friend Requests (receiverUid == currentUid && status == "PENDING")
            incomingRequestsRegistration = db.collection("friend_requests")
                .whereEqualTo("receiverUid", currentUid)
                .whereEqualTo("status", "PENDING")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error listening to incoming friend requests: ${error.message}")
                        return@addSnapshotListener
                    }
                    val incomingList = snapshot?.documents?.mapNotNull { doc ->
                        val senderUid = doc.getString("senderUid") ?: return@mapNotNull null
                        val senderName = doc.getString("senderName") ?: "Class 12 Student"
                        val avatarColor = (doc.getLong("senderAvatarColorHex")) ?: 0xFF00E5FF
                        Friend(
                            id = senderUid,
                            name = senderName,
                            avatarInitials = senderName.take(2).uppercase(),
                            avatarColorHex = avatarColor,
                            targetMarks = "90+ Marks",
                            completedTodoCount = 0,
                            studyStreakDays = 0,
                            competitionPoints = 0,
                            leaderboardRank = 0,
                            isOnline = false,
                            statusText = "Class 12 Commerce Student",
                            friendshipStatus = FriendshipStatus.PENDING_INCOMING
                        )
                    }.orEmpty()

                    updateFriendsList { current ->
                        val nonIncoming = current.filterNot { it.friendshipStatus == FriendshipStatus.PENDING_INCOMING }
                        nonIncoming + incomingList
                    }
                }

            // 2. Listen for Outgoing Friend Requests (senderUid == currentUid && status == "PENDING")
            outgoingRequestsRegistration = db.collection("friend_requests")
                .whereEqualTo("senderUid", currentUid)
                .whereEqualTo("status", "PENDING")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error listening to outgoing friend requests: ${error.message}")
                        return@addSnapshotListener
                    }
                    val outgoingList = snapshot?.documents?.mapNotNull { doc ->
                        val receiverUid = doc.getString("receiverUid") ?: return@mapNotNull null
                        val receiverName = doc.getString("receiverName") ?: "Class 12 Student"
                        val avatarColor = (doc.getLong("receiverAvatarColorHex")) ?: 0xFF00E5FF
                        Friend(
                            id = receiverUid,
                            name = receiverName,
                            avatarInitials = receiverName.take(2).uppercase(),
                            avatarColorHex = avatarColor,
                            targetMarks = "90+ Marks",
                            completedTodoCount = 0,
                            studyStreakDays = 0,
                            competitionPoints = 0,
                            leaderboardRank = 0,
                            isOnline = false,
                            statusText = "Invite Sent",
                            friendshipStatus = FriendshipStatus.PENDING_OUTGOING
                        )
                    }.orEmpty()

                    updateFriendsList { current ->
                        val nonOutgoing = current.filterNot { it.friendshipStatus == FriendshipStatus.PENDING_OUTGOING }
                        nonOutgoing + outgoingList
                    }
                }

            // 3. Listen for Accepted Friends in /students/{currentUid}/friends
            friendsListRegistration = db.collection("students")
                .document(currentUid)
                .collection("friends")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error listening to accepted friends: ${error.message}")
                        return@addSnapshotListener
                    }

                    val friendUids = snapshot?.documents?.mapNotNull { it.getString("friendUid") ?: it.id }.orEmpty()

                    // Remove listeners for removed friends
                    val currentTrackedUids = friendProfileRegistrations.keys.toSet()
                    (currentTrackedUids - friendUids.toSet()).forEach { removedUid ->
                        friendProfileRegistrations[removedUid]?.remove()
                        friendProfileRegistrations.remove(removedUid)
                        chatRegistrations[removedUid]?.remove()
                        chatRegistrations.remove(removedUid)
                    }

                    // Attach listener for each accepted friend to get real-time study progress
                    friendUids.forEach { friendUid ->
                        if (!friendProfileRegistrations.containsKey(friendUid)) {
                            listenToFriendProfile(friendUid)
                        }
                        if (!chatRegistrations.containsKey(friendUid)) {
                            listenToChatMessages(currentUid, friendUid)
                        }
                    }

                    // Remove any accepted friends no longer in the list
                    updateFriendsList { current ->
                        val pending = current.filter { it.friendshipStatus != FriendshipStatus.ACCEPTED }
                        val accepted = current.filter { it.friendshipStatus == FriendshipStatus.ACCEPTED && friendUids.contains(it.id) }
                        pending + accepted
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up Firestore listeners: ${e.message}", e)
        }
    }

    private fun listenToFriendProfile(friendUid: String) {
        val db = firestore ?: return
        val reg = db.collection("students").document(friendUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val data = snapshot.data ?: return@addSnapshotListener
                val name = (data["studentName"] as? String) ?: "Class 12 Student"
                val avatarColor = (data["avatarColorHex"] as? Number)?.toLong() ?: 0xFF00E5FF
                val todos = (data["completedTodoCount"] as? Number)?.toInt() ?: 0
                val streak = (data["studyStreakDays"] as? Number)?.toInt() ?: 0
                val points = (data["competitionPoints"] as? Number)?.toInt() ?: ((todos * 10) + (streak * 15))
                val target = (data["targetMarks"] as? String) ?: "90+ Marks"
                val isOnline = (data["isOnline"] as? Boolean) ?: false
                val statusText = (data["statusText"] as? String) ?: "Class 12 Commerce Student"
                val chapters = ((data["completedChapterIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()).toSet()

                val friendObj = Friend(
                    id = friendUid,
                    name = name,
                    avatarInitials = name.take(2).uppercase(),
                    avatarColorHex = avatarColor,
                    targetMarks = target,
                    completedTodoCount = todos,
                    studyStreakDays = streak,
                    competitionPoints = points,
                    leaderboardRank = 0,
                    isOnline = isOnline,
                    statusText = statusText,
                    completedChapterIds = chapters,
                    friendshipStatus = FriendshipStatus.ACCEPTED
                )

                updateFriendsList { current ->
                    val other = current.filterNot { it.id == friendUid }
                    other + friendObj
                }
            }
        friendProfileRegistrations[friendUid] = reg
    }

    private fun listenToChatMessages(currentUid: String, friendUid: String) {
        val db = firestore ?: return
        val chatId = if (currentUid < friendUid) "${currentUid}_${friendUid}" else "${friendUid}_${currentUid}"

        val reg = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val messages = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val senderId = doc.getString("senderId") ?: return@mapNotNull null
                    val text = doc.getString("text") ?: ""
                    val timestamp = (doc.getLong("timestamp")) ?: System.currentTimeMillis()
                    val formattedTime = doc.getString("formattedTime") ?: Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(TIME_FORMATTER)
                    val isRead = doc.getBoolean("isRead") ?: true
                    val reactionsMap = (doc.get("reactions") as? Map<*, *>)?.entries?.associate {
                        (it.key as String) to ((it.value as? Number)?.toInt() ?: 1)
                    } ?: emptyMap()

                    ChatMessage(
                        id = id,
                        friendId = friendUid,
                        text = text,
                        isFromMe = (senderId == currentUid),
                        timestamp = timestamp,
                        formattedTime = formattedTime,
                        isRead = isRead,
                        reactions = reactionsMap
                    )
                }

                _chatMessages.value = _chatMessages.value + (friendUid to messages)

                // Update unread count for this friend
                val unreadCount = messages.count { !it.isFromMe && !it.isRead }
                updateFriendsList { current ->
                    current.map { friend ->
                        if (friend.id == friendUid) friend.copy(unreadMessageCount = unreadCount) else friend
                    }
                }
            }
        chatRegistrations[friendUid] = reg
    }

    private fun updateFriendsList(transform: (List<Friend>) -> List<Friend>) {
        val updated = transform(_friends.value)
        _friends.value = updated.distinctBy { it.id }
    }

    private fun stopListeners() {
        incomingRequestsRegistration?.remove()
        incomingRequestsRegistration = null
        outgoingRequestsRegistration?.remove()
        outgoingRequestsRegistration = null
        friendsListRegistration?.remove()
        friendsListRegistration = null
        chatRegistrations.values.forEach { it.remove() }
        chatRegistrations.clear()
        friendProfileRegistrations.values.forEach { it.remove() }
        friendProfileRegistrations.clear()
    }

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

        val currentUid = currentProfile?.userId ?: return
        val currentName = currentProfile?.studentName ?: "Class 12 Student"
        val db = firestore ?: return

        repositoryScope.launch {
            try {
                val requestId1 = "${friendId}_${currentUid}"
                val requestId2 = "${currentUid}_${friendId}"

                // Update request status in Firestore
                val reqRef1 = db.collection("friend_requests").document(requestId1)
                val reqRef2 = db.collection("friend_requests").document(requestId2)

                val snap1 = reqRef1.get().awaitTask()
                if (snap1.exists()) {
                    reqRef1.update("status", "ACCEPTED")
                }
                val snap2 = reqRef2.get().awaitTask()
                if (snap2.exists()) {
                    reqRef2.update("status", "ACCEPTED")
                }

                // Mutual friendship creation in students/{uid}/friends
                db.collection("students")
                    .document(currentUid)
                    .collection("friends")
                    .document(friendId)
                    .set(mapOf("friendUid" to friendId, "addedAt" to System.currentTimeMillis()), SetOptions.merge())

                db.collection("students")
                    .document(friendId)
                    .collection("friends")
                    .document(currentUid)
                    .set(mapOf("friendUid" to currentUid, "friendName" to currentName, "addedAt" to System.currentTimeMillis()), SetOptions.merge())

                Log.d(TAG, "Mutual friendship accepted between $currentUid and $friendId")
            } catch (e: Exception) {
                Log.e(TAG, "Error accepting friend request in Firestore: ${e.message}", e)
            }
        }
    }

    /**
     * Reject or remove friend
     */
    fun removeFriend(friendId: String) {
        _friends.value = _friends.value.filterNot { it.id == friendId }

        val currentUid = currentProfile?.userId ?: return
        val db = firestore ?: return

        repositoryScope.launch {
            try {
                db.collection("students").document(currentUid).collection("friends").document(friendId).delete()
                db.collection("students").document(friendId).collection("friends").document(currentUid).delete()
                db.collection("friend_requests").document("${friendId}_${currentUid}").delete()
                db.collection("friend_requests").document("${currentUid}_${friendId}").delete()
                Log.d(TAG, "Removed friend $friendId for $currentUid")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing friend in Firestore: ${e.message}", e)
            }
        }
    }

    /**
     * Send friend invite by student name or code
     */
    fun sendFriendInvite(nameOrCode: String): Boolean {
        val cleanInput = nameOrCode.trim()
        if (cleanInput.isBlank()) return false

        val tempId = "invite_${UUID.randomUUID()}"
        val newFriend = Friend(
            id = tempId,
            name = cleanInput,
            avatarInitials = cleanInput.take(2).uppercase(),
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

        val currentUid = currentProfile?.userId
        val currentName = currentProfile?.studentName ?: "Class 12 Student"
        val currentAvatarColor = currentProfile?.avatarColorHex ?: 0xFF00E5FF
        val db = firestore

        if (db != null && !currentUid.isNullOrBlank()) {
            repositoryScope.launch {
                try {
                    // Search for registered student
                    var targetUid: String? = null
                    var targetName = cleanInput
                    var targetAvatarColor = 0xFF00E5FF

                    val directDoc = db.collection("students").document(cleanInput).get().awaitTask()
                    if (directDoc.exists()) {
                        targetUid = directDoc.id
                        targetName = directDoc.getString("studentName") ?: cleanInput
                        targetAvatarColor = directDoc.getLong("avatarColorHex") ?: 0xFF00E5FF
                    } else {
                        val nameMatch = db.collection("students")
                            .whereEqualTo("studentName", cleanInput)
                            .limit(1)
                            .get()
                            .awaitTask()
                        if (!nameMatch.isEmpty) {
                            val doc = nameMatch.documents.first()
                            targetUid = doc.id
                            targetName = doc.getString("studentName") ?: cleanInput
                            targetAvatarColor = doc.getLong("avatarColorHex") ?: 0xFF00E5FF
                        }
                    }

                    if (targetUid != null && targetUid != currentUid) {
                        val requestId = "${currentUid}_${targetUid}"
                        val requestDoc = mapOf(
                            "requestId" to requestId,
                            "senderUid" to currentUid,
                            "senderName" to currentName,
                            "senderAvatarColorHex" to currentAvatarColor,
                            "receiverUid" to targetUid,
                            "receiverName" to targetName,
                            "receiverAvatarColorHex" to targetAvatarColor,
                            "status" to "PENDING",
                            "createdAt" to System.currentTimeMillis()
                        )
                        db.collection("friend_requests").document(requestId).set(requestDoc, SetOptions.merge())
                        Log.d(TAG, "Friend request created in Firestore: $requestId")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Notice sending friend invite to Firestore: ${e.message}")
                }
            }
        }

        return true
    }

    /**
     * Send a 1-to-1 chat message to a friend
     */
    fun sendMessage(friendId: String, text: String): Boolean {
        if (text.isBlank()) return false
        val now = System.currentTimeMillis()
        val timeStr = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).format(TIME_FORMATTER)
        val messageId = UUID.randomUUID().toString()

        val newMessage = ChatMessage(
            id = messageId,
            friendId = friendId,
            text = text.trim(),
            isFromMe = true,
            timestamp = now,
            formattedTime = timeStr,
            isRead = true
        )

        val currentList = _chatMessages.value[friendId].orEmpty()
        _chatMessages.value = _chatMessages.value + (friendId to (currentList + newMessage))

        val currentUid = currentProfile?.userId
        val db = firestore

        if (db != null && !currentUid.isNullOrBlank()) {
            val chatId = if (currentUid < friendId) "${currentUid}_${friendId}" else "${friendId}_${currentUid}"
            repositoryScope.launch {
                try {
                    val messageData = mapOf(
                        "id" to messageId,
                        "senderId" to currentUid,
                        "receiverId" to friendId,
                        "text" to text.trim(),
                        "timestamp" to now,
                        "formattedTime" to timeStr,
                        "isRead" to false,
                        "reactions" to emptyMap<String, Int>()
                    )
                    db.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .document(messageId)
                        .set(messageData)
                } catch (e: Exception) {
                    Log.w(TAG, "Error sending message to Firestore: ${e.message}")
                }
            }
        }

        return true
    }

    /**
     * Toggle or add an emoji reaction on a message
     */
    fun toggleReaction(friendId: String, messageId: String, emoji: String) {
        val currentList = _chatMessages.value[friendId] ?: return
        var newReactionsMap: Map<String, Int> = emptyMap()

        val updated = currentList.map { msg ->
            if (msg.id == messageId) {
                val currentCount = msg.reactions[emoji] ?: 0
                val newReactions = if (currentCount > 0) {
                    msg.reactions - emoji
                } else {
                    msg.reactions + (emoji to 1)
                }
                newReactionsMap = newReactions
                msg.copy(reactions = newReactions)
            } else {
                msg
            }
        }
        _chatMessages.value = _chatMessages.value + (friendId to updated)

        val currentUid = currentProfile?.userId
        val db = firestore
        if (db != null && !currentUid.isNullOrBlank()) {
            val chatId = if (currentUid < friendId) "${currentUid}_${friendId}" else "${friendId}_${currentUid}"
            repositoryScope.launch {
                try {
                    db.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .document(messageId)
                        .update("reactions", newReactionsMap)
                } catch (e: Exception) {
                    Log.w(TAG, "Error updating reaction in Firestore: ${e.message}")
                }
            }
        }
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

        val currentUid = currentProfile?.userId
        val db = firestore
        if (db != null && !currentUid.isNullOrBlank()) {
            val chatId = if (currentUid < friendId) "${currentUid}_${friendId}" else "${friendId}_${currentUid}"
            repositoryScope.launch {
                try {
                    val unreadDocs = db.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .whereEqualTo("receiverId", currentUid)
                        .whereEqualTo("isRead", false)
                        .get()
                        .awaitTask()

                    unreadDocs.documents.forEach { doc ->
                        doc.reference.update("isRead", true)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error marking messages as read in Firestore: ${e.message}")
                }
            }
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
            avatarColorHex = currentStudentProfile.avatarColorHex,
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
        stopListeners()
        currentProfile = null
        _friends.value = emptyList()
        _chatMessages.value = emptyMap()
    }
}
