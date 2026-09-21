package com.example.core.repository

import com.example.core.model.StudentProfile

sealed interface AuthState {
    data object Initial : AuthState
    data object Loading : AuthState
    data class OtpSent(
        val phoneNumber: String,
        val studentName: String,
        val verificationId: String? = null
    ) : AuthState
    data class Authenticated(val profile: StudentProfile) : AuthState
    data class Error(val message: String, val canRetry: Boolean = true) : AuthState
}
