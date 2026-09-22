package com.example.core.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.core.constants.AppConstants
import com.example.core.firebase.FirebaseInitHelper
import com.example.core.model.StudentProfile
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

interface AuthRepository {
    val authState: StateFlow<AuthState>
    suspend fun checkExistingSession(): StudentProfile?
    suspend fun initiateGoogleSignIn(context: Context, studentName: String): Result<StudentProfile>
    suspend fun sendPhoneOtp(activity: Activity?, phoneNumber: String, studentName: String): Result<Unit>
    suspend fun verifyPhoneOtp(
        verificationId: String?,
        phoneNumber: String,
        otp: String,
        studentName: String
    ): Result<StudentProfile>
    suspend fun signOut()
    fun clearError()
}

class AuthRepositoryImpl(
    private val context: Context,
    private val profileRepository: StudentProfileRepository = FirestoreStudentProfileRepository(context)
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val prefs = context.getSharedPreferences("nexora_auth_prefs", Context.MODE_PRIVATE)

    private val isFirebaseAvailable: Boolean
        get() {
            FirebaseInitHelper.ensureInitialized(context)
            return try {
                FirebaseApp.getApps(context).isNotEmpty()
            } catch (e: Throwable) {
                false
            }
        }

    private val firebaseAuth: FirebaseAuth?
        get() {
            FirebaseInitHelper.ensureInitialized(context)
            return if (isFirebaseAvailable) {
                try {
                    FirebaseAuth.getInstance()
                } catch (e: Throwable) {
                    Log.w("AuthRepo", "FirebaseAuth.getInstance() failed: ${e.message}")
                    null
                }
            } else null
        }

    override suspend fun checkExistingSession(): StudentProfile? = withContext(Dispatchers.IO) {
        // 1. Check if Firebase user is logged in
        val currentFirebaseUser = firebaseAuth?.currentUser
        if (currentFirebaseUser != null) {
            val remoteProfile = profileRepository.getProfile(currentFirebaseUser.uid).getOrNull()
            if (remoteProfile != null) {
                val profileWithId = if (remoteProfile.studentId.isBlank()) {
                    profileRepository.ensureStudentId(remoteProfile)
                } else {
                    remoteProfile
                }
                cacheProfileLocally(profileWithId)
                _authState.value = AuthState.Authenticated(profileWithId)
                return@withContext profileWithId
            }
        }

        // 2. Check local cached session
        val cachedUserId = prefs.getString("user_id", null)
        val cachedName = prefs.getString("student_name", null)
        if (!cachedUserId.isNullOrEmpty() && !cachedName.isNullOrEmpty()) {
            var profile = StudentProfile(
                userId = cachedUserId,
                studentId = prefs.getString("student_id", null) ?: "",
                studentName = cachedName,
                email = prefs.getString("email", null),
                phoneNumber = prefs.getString("phone", null),
                medium = prefs.getString("medium", AppConstants.ACADEMIC_MEDIUM) ?: AppConstants.ACADEMIC_MEDIUM,
                classLevel = prefs.getString("class_level", AppConstants.ACADEMIC_STREAM) ?: AppConstants.ACADEMIC_STREAM,
                targetMarks = prefs.getString("target_marks", AppConstants.DEFAULT_TARGET_MARKS) ?: AppConstants.DEFAULT_TARGET_MARKS
            )
            if (profile.studentId.isBlank()) {
                profile = profileRepository.ensureStudentId(profile)
                cacheProfileLocally(profile)
            }
            _authState.value = AuthState.Authenticated(profile)
            return@withContext profile
        }

        null
    }

    override suspend fun initiateGoogleSignIn(context: Context, studentName: String): Result<StudentProfile> = withContext(Dispatchers.IO) {
        val trimmedName = studentName.trim()
        if (trimmedName.isEmpty()) {
            val error = "Please enter your student name to proceed."
            _authState.value = AuthState.Error(error)
            return@withContext Result.failure(IllegalArgumentException(error))
        }

        _authState.value = AuthState.Loading

        try {
            val auth = firebaseAuth ?: throw IllegalStateException("Firebase is not initialized.")
            val credentialManager = CredentialManager.create(context)

            // Look up Web Client ID from resources if generated by google-services or fallback
            val webClientId = try {
                val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                if (resId != 0) context.getString(resId) else null
            } catch (e: Exception) {
                null
            } ?: "340392504744-ibu5aqpaa9kemavqk0kncs8erk0mtvoa.apps.googleusercontent.com"

            val googleIdOptionBuilder = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)

            if (!webClientId.isNullOrBlank()) {
                googleIdOptionBuilder.setServerClientId(webClientId)
            }

            val googleIdOption = googleIdOptionBuilder.build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(context, request)
            val credential = response.credential

            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                if (idToken.isNotBlank()) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth.signInWithCredential(firebaseCredential).awaitTask()
                    val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user returned null after sign-in.")

                    val finalName = if (trimmedName.isNotEmpty()) trimmedName else (firebaseUser.displayName ?: "Class 12 Student")
                    val rawProfile = StudentProfile(
                        userId = firebaseUser.uid,
                        studentName = finalName,
                        email = firebaseUser.email,
                        phoneNumber = firebaseUser.phoneNumber,
                        medium = AppConstants.ACADEMIC_MEDIUM,
                        classLevel = AppConstants.ACADEMIC_STREAM,
                        targetMarks = AppConstants.DEFAULT_TARGET_MARKS
                    )

                    val profile = profileRepository.ensureStudentId(rawProfile)
                    profileRepository.saveProfile(profile)
                    cacheProfileLocally(profile)

                    _authState.value = AuthState.Authenticated(profile)
                    return@withContext Result.success(profile)
                } else {
                    throw IllegalStateException("Google ID token was empty.")
                }
            } else {
                throw IllegalStateException("Unsupported credential type returned from Google Sign-In.")
            }
        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
            Log.i("AuthRepo", "Google Sign-In cancelled by user")
            val errorMsg = "Google Sign-In was cancelled. You can sign in using Mobile Number + OTP."
            _authState.value = AuthState.Error(errorMsg)
            return@withContext Result.failure(e)
        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            Log.w("AuthRepo", "No Google credentials available on device: ${e.message}")
            val errorMsg = "No Google Account found on this device. Please sign in using Mobile Number + OTP below, or add a Google account in device Settings."
            _authState.value = AuthState.Error(errorMsg)
            return@withContext Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.e("AuthRepo", "Google Sign-In failed via CredentialManager", e)
            val errorMsg = "Google Sign-In unavailable on this device (${e.message ?: "no matching account"}). Please sign in with Mobile Number + OTP."
            _authState.value = AuthState.Error(errorMsg)
            return@withContext Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Google Sign-In error", e)
            val errorMsg = when {
                e is FirebaseException -> "Firebase Auth error: ${e.localizedMessage ?: e.message}"
                e.message?.contains("client_id", ignoreCase = true) == true -> "Google Client ID configuration missing. Please verify Firebase project setup."
                else -> "Google Sign-In error: ${e.localizedMessage ?: e.message}"
            }
            _authState.value = AuthState.Error(errorMsg)
            return@withContext Result.failure(e)
        }
    }

    override suspend fun sendPhoneOtp(
        activity: Activity?,
        phoneNumber: String,
        studentName: String
    ): Result<Unit> = withContext(Dispatchers.Main) {
        val trimmedName = studentName.trim()
        val cleanPhone = phoneNumber.trim().replace(" ", "").replace("-", "")

        if (trimmedName.isEmpty()) {
            val error = "Please enter your student name."
            _authState.value = AuthState.Error(error)
            return@withContext Result.failure(IllegalArgumentException(error))
        }

        if (cleanPhone.length != 10 || !cleanPhone.all { it.isDigit() }) {
            val error = "Please enter a valid 10-digit mobile number."
            _authState.value = AuthState.Error(error)
            return@withContext Result.failure(IllegalArgumentException(error))
        }

        _authState.value = AuthState.Loading

        val auth = firebaseAuth
        if (auth == null) {
            val error = "Firebase is not initialized. Please verify your connection."
            _authState.value = AuthState.Error(error)
            return@withContext Result.failure(IllegalStateException(error))
        }

        if (activity == null) {
            val error = "Activity context is required for SMS phone verification."
            _authState.value = AuthState.Error(error)
            return@withContext Result.failure(IllegalStateException(error))
        }

        val formattedPhone = if (cleanPhone.startsWith("+")) cleanPhone else "+91$cleanPhone"
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Instant verification / auto-retrieval
                    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val authResult = auth.signInWithCredential(credential).awaitTask()
                            val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user is null")
                            val rawProfile = StudentProfile(
                                userId = firebaseUser.uid,
                                studentName = trimmedName,
                                email = null,
                                phoneNumber = formattedPhone,
                                medium = AppConstants.ACADEMIC_MEDIUM,
                                classLevel = AppConstants.ACADEMIC_STREAM,
                                targetMarks = AppConstants.DEFAULT_TARGET_MARKS
                            )
                            val profile = profileRepository.ensureStudentId(rawProfile)
                            profileRepository.saveProfile(profile)
                            cacheProfileLocally(profile)
                            _authState.value = AuthState.Authenticated(profile)
                        } catch (e: Exception) {
                            Log.e("AuthRepo", "Auto-verification sign-in failed", e)
                            _authState.value = AuthState.Error("Auto-verification failed: ${e.localizedMessage ?: e.message}")
                        }
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("AuthRepo", "Firebase Phone Verification Failed: ${e.message}", e)
                    val friendlyMessage = when {
                        e.message?.contains("API key not valid", ignoreCase = true) == true ||
                        e.message?.contains("invalid API key", ignoreCase = true) == true ->
                            "Firebase API Key is invalid in google-services.json. Please download and replace google-services.json from Firebase Console with your project's active API key."
                        e.message?.contains("quota", ignoreCase = true) == true ->
                            "SMS quota exceeded for Firebase project. Please check SMS limits in Firebase Console or use Google Sign-In."
                        e.message?.contains("app-not-authorized", ignoreCase = true) == true ||
                        e.message?.contains("play integrity", ignoreCase = true) == true ||
                        e.message?.contains("recaptcha", ignoreCase = true) == true ||
                        e.message?.contains("sha", ignoreCase = true) == true ->
                            "App verification failed. Ensure SHA-1 fingerprint (A1:23:F0:E0:EF:99:2F:12:A3:07:AD:59:29:53:4F:76:A2:EF:BD:0A) is added in Firebase Console project settings."
                        e.message?.contains("invalid-phone-number", ignoreCase = true) == true ->
                            "The mobile number format is invalid. Please enter a valid 10-digit number."
                        e.message?.contains("network", ignoreCase = true) == true ->
                            "Network connection failed. Please check your internet connection and retry."
                        else ->
                            "Firebase Phone Verification error: ${e.localizedMessage ?: e.message}"
                    }
                    _authState.value = AuthState.Error(friendlyMessage)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d("AuthRepo", "SMS OTP sent. Verification ID: $verificationId")
                    _authState.value = AuthState.OtpSent(
                        phoneNumber = cleanPhone,
                        studentName = trimmedName,
                        verificationId = verificationId
                    )
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
        Result.success(Unit)
    }

    override suspend fun verifyPhoneOtp(
        verificationId: String?,
        phoneNumber: String,
        otp: String,
        studentName: String
    ): Result<StudentProfile> = withContext(Dispatchers.IO) {
        val cleanOtp = otp.trim()
        val cleanPhone = phoneNumber.trim().replace(" ", "").replace("-", "")
        if (cleanOtp.length != 6 || !cleanOtp.all { it.isDigit() }) {
            val error = "Please enter the complete 6-digit verification code."
            _authState.value = AuthState.Error(error)
            return@withContext Result.failure(IllegalArgumentException(error))
        }

        if (verificationId.isNullOrBlank()) {
            val error = "No active OTP verification session found. Please request a new OTP code."
            _authState.value = AuthState.Error(error)
            return@withContext Result.failure(IllegalStateException(error))
        }

        _authState.value = AuthState.Loading

        val auth = firebaseAuth
        if (auth == null) {
            val error = "Firebase is not initialized. Please check network connection."
            _authState.value = AuthState.Error(error)
            return@withContext Result.failure(IllegalStateException(error))
        }

        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, cleanOtp)
            val authResult = auth.signInWithCredential(credential).awaitTask()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user is null after OTP verification")
            val formattedPhone = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"

            val rawProfile = StudentProfile(
                userId = firebaseUser.uid,
                studentName = studentName.trim(),
                email = null,
                phoneNumber = formattedPhone,
                medium = AppConstants.ACADEMIC_MEDIUM,
                classLevel = AppConstants.ACADEMIC_STREAM,
                targetMarks = AppConstants.DEFAULT_TARGET_MARKS
            )

            // Ensure unique permanent studentId and save student profile in Firestore
            val profile = profileRepository.ensureStudentId(rawProfile)
            profileRepository.saveProfile(profile)
            cacheProfileLocally(profile)

            _authState.value = AuthState.Authenticated(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Firebase OTP verification failed", e)
            val error = "Incorrect verification code. Please check and try again (${e.localizedMessage ?: e.message})."
            _authState.value = AuthState.Error(error)
            Result.failure(e)
        }
    }

    override suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            firebaseAuth?.signOut()
        } catch (e: Throwable) {
            Log.w("AuthRepo", "Error signing out of Firebase: ${e.message}")
        }
        prefs.edit().clear().apply()
        _authState.value = AuthState.Initial
    }

    override fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Initial
        }
    }

    private fun cacheProfileLocally(profile: StudentProfile) {
        prefs.edit()
            .putString("user_id", profile.userId)
            .putString("student_id", profile.studentId)
            .putString("student_name", profile.studentName)
            .putString("email", profile.email)
            .putString("phone", profile.phoneNumber)
            .putString("medium", profile.medium)
            .putString("class_level", profile.classLevel)
            .putString("target_marks", profile.targetMarks)
            .apply()
    }
}

