package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.Friend
import com.example.core.model.PublicStudentProfile
import com.example.core.model.StudentProfile
import com.example.core.model.Subject
import com.example.core.repository.FriendsRepository
import com.example.core.repository.StudyPlannerRepository
import com.example.core.repository.SyllabusRepository
import com.example.ui.components.NexoraBottomNavigationBar
import com.example.ui.components.NexoraTab
import com.example.ui.screens.friends.CompetitionScreen
import com.example.ui.screens.friends.FriendChatScreen
import com.example.ui.screens.friends.FriendProfileScreen
import com.example.ui.screens.friends.FriendsScreen
import com.example.ui.theme.NexoraBackground

/**
 * Navigation state for full-screen overlay pages:
 * - None (Regular Tab views with bottom bar)
 * - Friends (Full-screen Friends list & requests)
 * - FriendProfile (Full-screen profile for specific student)
 * - Chat (Full-screen 1-to-1 chat with friend)
 * - Competition (Full-screen Top 10 + Your Rank leaderboard)
 */
sealed interface FullScreenRoute {
    data object None : FullScreenRoute
    data object Friends : FullScreenRoute
    data class FriendProfile(
        val profile: PublicStudentProfile,
        val returnRoute: FullScreenRoute = Friends
    ) : FullScreenRoute
    data class Chat(val friend: Friend) : FullScreenRoute
    data object Competition : FullScreenRoute
}

/**
 * Main application screen after login with clean bottom navigation:
 * - Home
 * - Subjects
 * - Planner
 * - Exam
 * - Profile
 *
 * Full-screen pages (Friends, Friend Profile, Chat, Competition, Subject Details)
 * open as dedicated full-screen pages without bottom navigation, preserving
 * the clean 5-tab bottom navigation structure.
 */
@Composable
fun MainAppScreen(
    studentProfile: StudentProfile,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(NexoraTab.HOME) }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var fullScreenRoute by remember { mutableStateOf<FullScreenRoute>(FullScreenRoute.None) }

    // Friends and study progress state for leaderboard and chat
    val friendsList by FriendsRepository.friends.collectAsStateWithLifecycle()
    val chatMessagesMap by FriendsRepository.chatMessages.collectAsStateWithLifecycle()
    val totalNotificationCount = remember(friendsList) { FriendsRepository.getTotalNotificationCount() }
    val tasks by StudyPlannerRepository.tasks.collectAsStateWithLifecycle()
    val streak = remember(tasks) { StudyPlannerRepository.getStudyStreak() }
    val allCompletedTasksCount = remember(tasks) { tasks.count { it.isCompleted } }
    val currentStudentCompletedChapters by SyllabusRepository.completedChapterIds.collectAsStateWithLifecycle()

    val isFullScreenActive = selectedSubject != null || fullScreenRoute !is FullScreenRoute.None
    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext

    // Initialize FriendsRepository with current student
    androidx.compose.runtime.LaunchedEffect(studentProfile.userId) {
        FriendsRepository.initialize(studentProfile)
    }

    // Sync current student's study progress to Firestore
    androidx.compose.runtime.LaunchedEffect(allCompletedTasksCount, streak.currentStreak, currentStudentCompletedChapters) {
        if (studentProfile.userId.isNotBlank()) {
            com.example.core.repository.FirestoreStudentProfileRepository(context).syncStudentProgress(
                userId = studentProfile.userId,
                completedTodos = allCompletedTasksCount,
                streakDays = streak.currentStreak,
                completedChapters = currentStudentCompletedChapters.toList()
            )
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_app_screen"),
        containerColor = NexoraBackground,
        bottomBar = {
            // Bottom navigation bar is ONLY visible when on normal tab screens
            if (!isFullScreenActive) {
                NexoraBottomNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                        selectedSubject = null
                        fullScreenRoute = FullScreenRoute.None
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NexoraBackground)
                .padding(if (isFullScreenActive) androidx.compose.foundation.layout.PaddingValues() else innerPadding)
        ) {
            // 1. Full-screen Subject Detail View
            if (selectedSubject != null) {
                SubjectDetailScreen(
                    subject = selectedSubject!!,
                    onBackClick = { selectedSubject = null }
                )
            }
            // 2. Full-screen Friends, Chat, Profile & Competition Pages
            else if (fullScreenRoute !is FullScreenRoute.None) {
                when (val route = fullScreenRoute) {
                    is FullScreenRoute.Friends -> {
                        FriendsScreen(
                            friends = friendsList,
                            totalNotificationCount = totalNotificationCount,
                            onBackClick = { fullScreenRoute = FullScreenRoute.None },
                            onFriendClick = { friend ->
                                val publicProfile = PublicStudentProfile(
                                    studentId = friend.id,
                                    studentName = friend.name,
                                    avatarInitials = friend.avatarInitials,
                                    avatarColorHex = friend.avatarColorHex,
                                    targetMarks = friend.targetMarks,
                                    completedTodoCount = friend.completedTodoCount,
                                    streakDays = friend.studyStreakDays,
                                    competitionPoints = friend.competitionPoints,
                                    leaderboardRank = friend.leaderboardRank,
                                    isOnline = friend.isOnline,
                                    isCurrentStudent = false,
                                    statusText = friend.statusText,
                                    completedChapterIds = friend.completedChapterIds
                                )
                                fullScreenRoute = FullScreenRoute.FriendProfile(
                                    profile = publicProfile,
                                    returnRoute = FullScreenRoute.Friends
                                )
                            },
                            onOpenChat = { friend ->
                                FriendsRepository.markChatAsRead(friend.id)
                                fullScreenRoute = FullScreenRoute.Chat(friend)
                            },
                            onOpenCompetition = {
                                fullScreenRoute = FullScreenRoute.Competition
                            }
                        )
                    }
                    is FullScreenRoute.FriendProfile -> {
                        val matchingFriend = friendsList.find { it.id == route.profile.studentId }
                        FriendProfileScreen(
                            profile = route.profile,
                            onBackClick = {
                                fullScreenRoute = route.returnRoute
                            },
                            onOpenChat = if (matchingFriend != null) {
                                {
                                    FriendsRepository.markChatAsRead(matchingFriend.id)
                                    fullScreenRoute = FullScreenRoute.Chat(matchingFriend)
                                }
                            } else null
                        )
                    }
                    is FullScreenRoute.Chat -> {
                        val messages = chatMessagesMap[route.friend.id].orEmpty()
                        FriendChatScreen(
                            friend = route.friend,
                            messages = messages,
                            onBackClick = {
                                fullScreenRoute = FullScreenRoute.Friends
                            },
                            onSendMessage = { text ->
                                FriendsRepository.sendMessage(route.friend.id, text)
                            },
                            onToggleReaction = { messageId, emoji ->
                                FriendsRepository.toggleReaction(route.friend.id, messageId, emoji)
                            },
                            onOpenFriendProfile = {
                                val publicProfile = PublicStudentProfile(
                                    studentId = route.friend.id,
                                    studentName = route.friend.name,
                                    avatarInitials = route.friend.avatarInitials,
                                    avatarColorHex = route.friend.avatarColorHex,
                                    targetMarks = route.friend.targetMarks,
                                    completedTodoCount = route.friend.completedTodoCount,
                                    streakDays = route.friend.studyStreakDays,
                                    competitionPoints = route.friend.competitionPoints,
                                    leaderboardRank = route.friend.leaderboardRank,
                                    isOnline = route.friend.isOnline,
                                    isCurrentStudent = false,
                                    statusText = route.friend.statusText,
                                    completedChapterIds = route.friend.completedChapterIds
                                )
                                fullScreenRoute = FullScreenRoute.FriendProfile(
                                    profile = publicProfile,
                                    returnRoute = FullScreenRoute.Chat(route.friend)
                                )
                            }
                        )
                    }
                    is FullScreenRoute.Competition -> {
                        val (top10, myEntry) = remember(
                            friendsList,
                            allCompletedTasksCount,
                            streak.currentStreak,
                            currentStudentCompletedChapters
                        ) {
                            FriendsRepository.getLeaderboard(
                                currentStudentProfile = studentProfile,
                                studentCompletedTodos = allCompletedTasksCount,
                                studentStreakDays = streak.currentStreak
                            )
                        }
                        CompetitionScreen(
                            top10Entries = top10,
                            myEntry = myEntry,
                            onBackClick = {
                                fullScreenRoute = FullScreenRoute.Friends
                            },
                            onStudentClick = { entry ->
                                val publicProfile = if (entry.isCurrentStudent) {
                                    PublicStudentProfile(
                                        studentId = entry.studentId,
                                        studentName = entry.studentName,
                                        avatarInitials = entry.avatarInitials,
                                        avatarColorHex = entry.avatarColorHex,
                                        targetMarks = studentProfile.targetMarks,
                                        completedTodoCount = entry.completedTodoCount,
                                        streakDays = entry.streakDays,
                                        competitionPoints = entry.competitionPoints,
                                        leaderboardRank = entry.rank,
                                        isOnline = true,
                                        isCurrentStudent = true,
                                        statusText = "Active Now",
                                        completedChapterIds = currentStudentCompletedChapters
                                    )
                                } else {
                                    val friend = friendsList.find { it.id == entry.studentId }
                                    PublicStudentProfile(
                                        studentId = entry.studentId,
                                        studentName = entry.studentName,
                                        avatarInitials = entry.avatarInitials,
                                        avatarColorHex = entry.avatarColorHex,
                                        targetMarks = friend?.targetMarks ?: "90+ Marks Target",
                                        completedTodoCount = entry.completedTodoCount,
                                        streakDays = entry.streakDays,
                                        competitionPoints = entry.competitionPoints,
                                        leaderboardRank = entry.rank,
                                        isOnline = friend?.isOnline ?: false,
                                        isCurrentStudent = false,
                                        statusText = friend?.statusText ?: "Class 12 Commerce Student",
                                        completedChapterIds = entry.completedChapterIds.ifEmpty {
                                            friend?.completedChapterIds ?: emptySet()
                                        }
                                    )
                                }
                                fullScreenRoute = FullScreenRoute.FriendProfile(
                                    profile = publicProfile,
                                    returnRoute = FullScreenRoute.Competition
                                )
                            }
                        )
                    }
                    is FullScreenRoute.None -> Unit
                }
            }
            // 3. Tab Screens (Home, Planner, Exam, Subjects, Profile)
            else {
                when (currentTab) {
                    NexoraTab.HOME -> {
                        HomeScreen(
                            studentProfile = studentProfile,
                            onSignOut = onSignOut,
                            onSubjectClick = { subject ->
                                selectedSubject = subject
                            },
                            onNavigateToPlanner = {
                                currentTab = NexoraTab.PLANNER
                            },
                            onNavigateToExam = {
                                currentTab = NexoraTab.EXAM
                            },
                            onOpenFriendsPage = {
                                fullScreenRoute = FullScreenRoute.Friends
                            }
                        )
                    }
                    NexoraTab.PLANNER -> {
                        StudyPlannerScreen(
                            onNavigateToSubjects = {
                                currentTab = NexoraTab.SUBJECTS
                            }
                        )
                    }
                    NexoraTab.EXAM -> {
                        ExamDashboardScreen(
                            onSubjectClick = { subject ->
                                selectedSubject = subject
                            },
                            onNavigateToPlanner = {
                                currentTab = NexoraTab.PLANNER
                            }
                        )
                    }
                    NexoraTab.SUBJECTS -> {
                        SubjectsScreen(
                            onSubjectClick = { subject ->
                                selectedSubject = subject
                            }
                        )
                    }
                    NexoraTab.PROFILE -> {
                        ProfileScreen(
                            studentProfile = studentProfile,
                            onSignOut = onSignOut
                        )
                    }
                }
            }
        }
    }
}
