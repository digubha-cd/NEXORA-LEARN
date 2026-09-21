package com.example.core.model

/**
 * Student profile representation for NEXORA LEARN.
 */
data class StudentProfile(
    val userId: String = "",
    val studentName: String = "",
    val email: String? = null,
    val phoneNumber: String? = null,
    val medium: String = "Gujarati Medium",
    val classLevel: String = "Class 12 Commerce",
    val targetMarks: String = "90+ Marks",
    val avatarColorHex: Long = 0xFF00E5FF,
    val completedTodoCount: Int = 0,
    val studyStreakDays: Int = 0,
    val competitionPoints: Int = 0,
    val completedChapterIds: List<String> = emptyList(),
    val isOnline: Boolean = true,
    val statusText: String = "Class 12 Commerce",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "studentName" to studentName,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "medium" to medium,
            "classLevel" to classLevel,
            "targetMarks" to targetMarks,
            "avatarColorHex" to avatarColorHex,
            "completedTodoCount" to completedTodoCount,
            "studyStreakDays" to studyStreakDays,
            "competitionPoints" to competitionPoints,
            "completedChapterIds" to completedChapterIds,
            "isOnline" to isOnline,
            "statusText" to statusText,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromFirestoreMap(map: Map<String, Any?>): StudentProfile {
            val todos = (map["completedTodoCount"] as? Number)?.toInt() ?: 0
            val streak = (map["studyStreakDays"] as? Number)?.toInt() ?: 0
            val points = (map["competitionPoints"] as? Number)?.toInt() ?: ((todos * 10) + (streak * 15))
            val chapters = (map["completedChapterIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            return StudentProfile(
                userId = map["userId"] as? String ?: "",
                studentName = map["studentName"] as? String ?: "",
                email = map["email"] as? String,
                phoneNumber = map["phoneNumber"] as? String,
                medium = map["medium"] as? String ?: "Gujarati Medium",
                classLevel = map["classLevel"] as? String ?: "Class 12 Commerce",
                targetMarks = map["targetMarks"] as? String ?: "90+ Marks",
                avatarColorHex = (map["avatarColorHex"] as? Number)?.toLong() ?: 0xFF00E5FF,
                completedTodoCount = todos,
                studyStreakDays = streak,
                competitionPoints = points,
                completedChapterIds = chapters,
                isOnline = (map["isOnline"] as? Boolean) ?: false,
                statusText = map["statusText"] as? String ?: "Class 12 Commerce",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

