package com.example.core.model

import java.util.UUID

/**
 * Public study profile representation in NEXORA LEARN.
 *
 * Privacy Guarantees:
 * - Only student name, avatar, 90+ target, overall & subject-wise study progress,
 *   chapter points, completed chapter names, pending chapter names, completed To-Do count,
 *   study streak, competition points, and leaderboard rank are visible.
 * - Mobile number, Gmail, login credentials, and account details are strictly private and NEVER exposed.
 */
data class PublicStudentProfile(
    val studentId: String = UUID.randomUUID().toString(),
    val studentName: String,
    val avatarInitials: String = studentName.take(2).uppercase(),
    val avatarColorHex: Long = 0xFF00E5FF,
    val targetMarks: String = "90+ Marks",
    val completedTodoCount: Int = 0,
    val streakDays: Int = 0,
    val competitionPoints: Int = 0,
    val leaderboardRank: Int = 0,
    val isOnline: Boolean = false,
    val isCurrentStudent: Boolean = false,
    val statusText: String = "Class 12 Commerce",
    val completedChapterIds: Set<String> = emptySet()
)
