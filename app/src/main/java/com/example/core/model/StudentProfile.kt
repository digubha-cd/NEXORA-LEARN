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
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromFirestoreMap(map: Map<String, Any?>): StudentProfile {
            return StudentProfile(
                userId = map["userId"] as? String ?: "",
                studentName = map["studentName"] as? String ?: "",
                email = map["email"] as? String,
                phoneNumber = map["phoneNumber"] as? String,
                medium = map["medium"] as? String ?: "Gujarati Medium",
                classLevel = map["classLevel"] as? String ?: "Class 12 Commerce",
                targetMarks = map["targetMarks"] as? String ?: "90+ Marks",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

