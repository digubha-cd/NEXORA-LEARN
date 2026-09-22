package com.example.core.util

import java.security.SecureRandom

/**
 * Utility for generating secure, human-friendly, unique NEXORA Student IDs.
 * Format: NX-XXXXXX (e.g. NX-7K4P92, NX-M8Q2LA)
 *
 * Uses uppercase alphanumeric characters excluding ambiguous characters (0, O, 1, I)
 * to avoid student transcription errors when sharing IDs.
 */
object StudentIdGenerator {

    private const val PREFIX = "NX-"
    private const val ID_LENGTH = 6
    // Clean character set without 0, O, 1, I, L to prevent human confusion
    private const val ALLOWED_CHARS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"
    private val random = SecureRandom()

    /**
     * Generate a candidate Student ID with the format NX-XXXXXX.
     */
    fun generateCandidateId(): String {
        val sb = StringBuilder(PREFIX)
        for (i in 0 until ID_LENGTH) {
            val randomIndex = random.nextInt(ALLOWED_CHARS.length)
            sb.append(ALLOWED_CHARS[randomIndex])
        }
        return sb.toString()
    }

    /**
     * Normalize and validate an input string as a potential Student ID.
     * Handles inputs with or without the "NX-" prefix and case insensitivity.
     * Returns formatted ID like "NX-XXXXXX" if valid, or null if invalid format.
     */
    fun normalizeStudentId(input: String?): String? {
        if (input.isNullOrBlank()) return null
        val trimmed = input.trim().uppercase()

        val rawCode = if (trimmed.startsWith(PREFIX)) {
            trimmed.removePrefix(PREFIX).replace("-", "").trim()
        } else if (trimmed.startsWith("NX")) {
            trimmed.removePrefix("NX").replace("-", "").trim()
        } else {
            trimmed.replace("-", "").trim()
        }

        // Must be exactly 6 alphanumeric characters
        if (rawCode.length != 6 || !rawCode.all { it.isLetterOrDigit() }) {
            return null
        }

        return "$PREFIX$rawCode"
    }

    /**
     * Check if a string matches the standard NEXORA Student ID format.
     */
    fun isValidStudentId(id: String?): Boolean {
        return normalizeStudentId(id) != null
    }
}
