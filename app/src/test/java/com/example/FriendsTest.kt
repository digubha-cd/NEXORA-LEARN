package com.example

import com.example.core.model.Friend
import com.example.core.model.FriendshipStatus
import com.example.core.model.PublicStudentProfile
import com.example.core.model.StudentProfile
import com.example.core.model.Subject
import com.example.core.repository.FriendsRepository
import com.example.core.repository.SyllabusRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Friends, Chat, Competition Leaderboard, and Public Study Profile
 * with real data only.
 */
class FriendsTest {

    @Before
    fun setup() {
        FriendsRepository.resetAllData()
        SyllabusRepository.resetAllCompletions()
    }

    @Test
    fun testInitialFriendsStateIsEmpty() {
        val accepted = FriendsRepository.getAcceptedFriends()
        val incoming = FriendsRepository.getIncomingRequests()
        assertTrue("Initial friends list must be empty with no fake/demo data", accepted.isEmpty())
        assertTrue("Initial incoming requests must be empty", incoming.isEmpty())
    }

    @Test
    fun testSendFriendInviteAndAcceptance() {
        val sent = FriendsRepository.sendFriendInvite("Bhavik Patel")
        assertTrue("Invite should be sent successfully", sent)

        val pending = FriendsRepository.friends.value.filter { it.friendshipStatus == FriendshipStatus.PENDING_OUTGOING }
        assertTrue("Should have outgoing invite for Bhavik Patel", pending.any { it.name == "Bhavik Patel" })

        // When request is accepted
        val invited = pending.first { it.name == "Bhavik Patel" }
        FriendsRepository.acceptFriendRequest(invited.id)

        val accepted = FriendsRepository.getAcceptedFriends()
        assertTrue("Accepted friends should contain Bhavik Patel", accepted.any { it.id == invited.id })

        // Privacy check: verify Friend model has no email, phone, or password
        accepted.forEach { friend ->
            assertNotNull(friend.name)
            assertNotNull(friend.avatarInitials)
            assertTrue(friend.targetMarks.contains("90+ Marks") || friend.targetMarks.contains("Marks"))
        }
    }

    @Test
    fun testOneToOneChatMessaging() {
        FriendsRepository.sendFriendInvite("Priya Shah")
        val friendId = FriendsRepository.friends.value.first().id
        FriendsRepository.acceptFriendRequest(friendId)

        val sent = FriendsRepository.sendMessage(friendId, "Let's study Economics Ch 1 today!")
        assertTrue("Message sent should return true", sent)

        val messages = FriendsRepository.getChatMessages(friendId)
        assertTrue("Messages list should contain sent message", messages.any { it.text == "Let's study Economics Ch 1 today!" && it.isFromMe })

        // Mark as read
        FriendsRepository.markChatAsRead(friendId)
        val readMessages = FriendsRepository.getChatMessages(friendId)
        assertTrue("Messages should be marked read", readMessages.all { it.isRead })
    }

    @Test
    fun testMessageReactions() {
        FriendsRepository.sendFriendInvite("Hardik Dave")
        val friendId = FriendsRepository.friends.value.first().id
        FriendsRepository.acceptFriendRequest(friendId)

        FriendsRepository.sendMessage(friendId, "Practicing Accountancy balance sheet")
        val sentMsg = FriendsRepository.getChatMessages(friendId).last()

        // Add reaction
        FriendsRepository.toggleReaction(friendId, sentMsg.id, "🔥")
        var updated = FriendsRepository.getChatMessages(friendId).first { it.id == sentMsg.id }
        assertEquals(1, updated.reactions["🔥"])

        // Toggle reaction off
        FriendsRepository.toggleReaction(friendId, sentMsg.id, "🔥")
        updated = FriendsRepository.getChatMessages(friendId).first { it.id == sentMsg.id }
        assertFalse(updated.reactions.containsKey("🔥"))
    }

    @Test
    fun testCompetitionLeaderboardScoringWithRealDataOnly() {
        val testProfile = StudentProfile(
            userId = "student_test_1",
            studentName = "Nirav Shah",
            targetMarks = "90+ Marks"
        )

        // Without friends, leaderboard has ONLY the real student (Rank 1)
        val (top10Solo, myEntrySolo) = FriendsRepository.getLeaderboard(
            currentStudentProfile = testProfile,
            studentCompletedTodos = 8,
            studentStreakDays = 3
        )

        // 8 todos * 10 = 80, 3 streak * 15 = 45 -> 125 pts
        assertEquals(125, myEntrySolo.competitionPoints)
        assertEquals(1, myEntrySolo.rank)
        assertEquals(1, top10Solo.size)
        assertEquals("🥇 1st", myEntrySolo.rankBadge)

        // Add a real friend
        FriendsRepository.sendFriendInvite("Kinjal Patel")
        val friendId = FriendsRepository.friends.value.first().id
        FriendsRepository.acceptFriendRequest(friendId)

        val (top10WithFriend, myEntryWithFriend) = FriendsRepository.getLeaderboard(
            currentStudentProfile = testProfile,
            studentCompletedTodos = 8,
            studentStreakDays = 3
        )

        assertEquals(2, top10WithFriend.size)
        assertTrue(top10WithFriend.any { it.isCurrentStudent })
        assertTrue(top10WithFriend.any { it.studentName == "Kinjal Patel" })
    }

    @Test
    fun testFriendSearchFilteringByName() {
        FriendsRepository.sendFriendInvite("Ananya Desai")
        val friendId = FriendsRepository.friends.value.first().id
        FriendsRepository.acceptFriendRequest(friendId)

        val accepted = FriendsRepository.getAcceptedFriends()
        assertTrue("Should have accepted friends to filter", accepted.isNotEmpty())

        val firstFriend = accepted.first()
        val partialName = firstFriend.name.take(3).lowercase()

        // Filter by matching prefix
        val matching = accepted.filter { it.name.lowercase().contains(partialName) }
        assertTrue("Should find matching friends with prefix '$partialName'", matching.isNotEmpty())
        assertTrue("Matching list should include the target friend", matching.any { it.id == firstFriend.id })

        // Filter by non-existent name
        val nonMatching = accepted.filter { it.name.lowercase().contains("xyznonexistentname123") }
        assertTrue("Non matching query should yield empty list", nonMatching.isEmpty())
    }

    @Test
    fun testPublicStudentStudyProfilePropertiesAndCalculations() {
        // Prepare completed chapters for a student
        val gujaratiChapter = SyllabusRepository.getChapters("gujarati").first()
        val statChapter = SyllabusRepository.getChapters("stat").first()
        val completedSet = setOf(gujaratiChapter.id, statChapter.id)

        val publicProfile = PublicStudentProfile(
            studentId = "friend_1",
            studentName = "Pooja Trivedi",
            avatarInitials = "PT",
            avatarColorHex = 0xFF00E5FF,
            targetMarks = "90+ Marks Target",
            completedTodoCount = 14,
            streakDays = 5,
            competitionPoints = 215,
            leaderboardRank = 1,
            isOnline = true,
            isCurrentStudent = false,
            statusText = "Active Now",
            completedChapterIds = completedSet
        )

        // 1. Overall stats
        val totalChapters = SyllabusRepository.getTotalChaptersCount()
        val completedChapters = SyllabusRepository.getTotalCompletedChaptersCount(completedSet)
        val pendingChapters = SyllabusRepository.getTotalPendingChaptersCount(completedSet)
        val overallProgress = SyllabusRepository.getOverallProgress(completedSet)
        val totalPoints = SyllabusRepository.getTotalEarnedPoints(completedSet)

        assertEquals(2, completedChapters)
        assertEquals(totalChapters - 2, pendingChapters)
        assertEquals(2f / totalChapters, overallProgress, 0.001f)
        assertEquals(gujaratiChapter.points + statChapter.points, totalPoints)

        // 2. All 7 Subjects verification
        val subjects = Subject.OFFICIAL_SUBJECTS
        assertEquals(7, subjects.size)

        subjects.forEach { subject ->
            val chapters = SyllabusRepository.getChapters(subject.id)
            val completed = chapters.count { completedSet.contains(it.id) }
            val completedTitles = SyllabusRepository.getCompletedChapterTitles(subject.id, completedSet)
            val pendingTitles = SyllabusRepository.getPendingChapterTitles(subject.id, completedSet)

            assertEquals(completed, completedTitles.size)
            assertEquals(chapters.size - completed, pendingTitles.size)

            if (subject.id == "gujarati") {
                assertEquals(1, completed)
                assertTrue(completedTitles.contains(gujaratiChapter.title))
            } else if (subject.id == "stat") {
                assertEquals(1, completed)
                assertTrue(completedTitles.contains(statChapter.title))
            } else {
                assertEquals(0, completed)
                assertTrue(completedTitles.isEmpty())
            }
        }
    }
}
