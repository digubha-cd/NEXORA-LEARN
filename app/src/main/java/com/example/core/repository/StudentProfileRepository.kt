package com.example.core.repository

import android.content.Context
import android.util.Log
import com.example.core.firebase.FirebaseInitHelper
import com.example.core.model.StudentProfile
import com.example.core.util.StudentIdGenerator
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.awaitTask(): T =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }

interface StudentProfileRepository {
    suspend fun saveProfile(profile: StudentProfile): Result<Unit>
    suspend fun getProfile(userId: String): Result<StudentProfile?>
    suspend fun ensureStudentId(profile: StudentProfile): StudentProfile
    suspend fun getStudentByStudentId(studentId: String): Result<StudentProfile?>
    suspend fun searchStudents(query: String): Result<List<StudentProfile>>
    suspend fun syncStudentProgress(
        userId: String,
        completedTodos: Int,
        streakDays: Int,
        completedChapters: List<String>
    ): Result<Unit>
}

class FirestoreStudentProfileRepository(
    private val context: Context
) : StudentProfileRepository {

    private val isFirebaseAvailable: Boolean
        get() {
            FirebaseInitHelper.ensureInitialized(context)
            return try {
                FirebaseApp.getApps(context).isNotEmpty()
            } catch (e: Throwable) {
                false
            }
        }

    override suspend fun ensureStudentId(profile: StudentProfile): StudentProfile = withContext(Dispatchers.IO) {
        if (profile.studentId.isNotBlank() && StudentIdGenerator.isValidStudentId(profile.studentId)) {
            // Ensure index document exists in student_ids collection
            if (isFirebaseAvailable && profile.userId.isNotBlank()) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("student_ids")
                        .document(profile.studentId)
                        .set(
                            mapOf(
                                "studentId" to profile.studentId,
                                "userId" to profile.userId,
                                "createdAt" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        ).awaitTask()
                } catch (e: Exception) {
                    Log.w("FirestoreRepo", "Error writing student_ids index: ${e.message}")
                }
            }
            return@withContext profile
        }

        if (!isFirebaseAvailable || profile.userId.isBlank()) {
            val fallbackId = StudentIdGenerator.generateCandidateId()
            return@withContext profile.copy(studentId = fallbackId)
        }

        val db = FirebaseFirestore.getInstance()

        // 1. Check if the student document already has a permanent studentId
        try {
            val docSnap = db.collection("students").document(profile.userId).get().awaitTask()
            if (docSnap.exists()) {
                val existingId = docSnap.getString("studentId")
                if (!existingId.isNullOrBlank() && StudentIdGenerator.isValidStudentId(existingId)) {
                    // Make sure index exists
                    db.collection("student_ids").document(existingId).set(
                        mapOf(
                            "studentId" to existingId,
                            "userId" to profile.userId,
                            "createdAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    ).awaitTask()
                    return@withContext profile.copy(studentId = existingId)
                }
            }
        } catch (e: Exception) {
            Log.w("FirestoreRepo", "Error checking existing studentId in Firestore: ${e.message}")
        }

        // 2. Generate unique student ID with collision detection & safe registration
        var assignedId: String? = null
        for (attempt in 1..10) {
            val candidate = StudentIdGenerator.generateCandidateId()
            try {
                val idDoc = db.collection("student_ids").document(candidate).get().awaitTask()
                if (!idDoc.exists()) {
                    // Safe to claim this unique ID
                    db.collection("student_ids").document(candidate).set(
                        mapOf(
                            "studentId" to candidate,
                            "userId" to profile.userId,
                            "createdAt" to System.currentTimeMillis()
                        )
                    ).awaitTask()
                    assignedId = candidate
                    break
                } else {
                    val owner = idDoc.getString("userId")
                    if (owner == profile.userId) {
                        assignedId = candidate
                        break
                    }
                    Log.w("FirestoreRepo", "Student ID collision on $candidate! Retrying attempt $attempt...")
                }
            } catch (e: Exception) {
                Log.e("FirestoreRepo", "Error checking candidate ID $candidate: ${e.message}")
                assignedId = candidate
                break
            }
        }

        val finalStudentId = assignedId ?: StudentIdGenerator.generateCandidateId()
        val updated = profile.copy(studentId = finalStudentId)

        try {
            db.collection("students").document(profile.userId).set(
                mapOf("studentId" to finalStudentId),
                SetOptions.merge()
            ).awaitTask()
            Log.d("FirestoreRepo", "Permanent unique Student ID assigned: $finalStudentId for user: ${profile.userId}")
        } catch (e: Exception) {
            Log.w("FirestoreRepo", "Error saving assigned studentId to students document: ${e.message}")
        }

        return@withContext updated
    }

    override suspend fun saveProfile(profile: StudentProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val resolvedProfile = if (profile.studentId.isBlank()) {
                ensureStudentId(profile)
            } else {
                profile
            }

            if (isFirebaseAvailable && resolvedProfile.userId.isNotBlank()) {
                val db = FirebaseFirestore.getInstance()
                db.collection("students")
                    .document(resolvedProfile.userId)
                    .set(resolvedProfile.toFirestoreMap(), SetOptions.merge())
                    .awaitTask()

                if (resolvedProfile.studentId.isNotBlank()) {
                    db.collection("student_ids")
                        .document(resolvedProfile.studentId)
                        .set(
                            mapOf(
                                "studentId" to resolvedProfile.studentId,
                                "userId" to resolvedProfile.userId,
                                "createdAt" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        ).awaitTask()
                }

                Log.d("FirestoreRepo", "Student profile saved in Firestore for userId: ${resolvedProfile.userId} with studentId: ${resolvedProfile.studentId}")
            } else {
                Log.i("FirestoreRepo", "Firebase not yet initialized. Saved locally in session.")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error saving profile to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getProfile(userId: String): Result<StudentProfile?> = withContext(Dispatchers.IO) {
        try {
            if (isFirebaseAvailable && userId.isNotBlank()) {
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("students")
                    .document(userId)
                    .get()
                    .awaitTask()

                if (snapshot.exists()) {
                    val data = snapshot.data
                    if (data != null) {
                        val profile = StudentProfile.fromFirestoreMap(data)
                        return@withContext Result.success(profile)
                    }
                }
            }
            Result.success(null)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error getting profile from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getStudentByStudentId(studentId: String): Result<StudentProfile?> = withContext(Dispatchers.IO) {
        val normalized = StudentIdGenerator.normalizeStudentId(studentId) ?: studentId.trim().uppercase()
        if (normalized.isBlank()) return@withContext Result.success(null)

        try {
            if (isFirebaseAvailable) {
                val db = FirebaseFirestore.getInstance()

                // 1. Direct index lookup in student_ids collection
                val idDoc = db.collection("student_ids").document(normalized).get().awaitTask()
                if (idDoc.exists()) {
                    val userId = idDoc.getString("userId")
                    if (!userId.isNullOrBlank()) {
                        val userDoc = db.collection("students").document(userId).get().awaitTask()
                        if (userDoc.exists()) {
                            userDoc.data?.let { data ->
                                val profile = StudentProfile.fromFirestoreMap(data)
                                val finalProfile = if (profile.studentId.isBlank()) profile.copy(studentId = normalized) else profile
                                return@withContext Result.success(finalProfile)
                            }
                        }
                    }
                }

                // 2. Query students collection by studentId field
                val querySnap = db.collection("students")
                    .whereEqualTo("studentId", normalized)
                    .limit(1)
                    .get()
                    .awaitTask()

                if (!querySnap.isEmpty) {
                    val doc = querySnap.documents.first()
                    doc.data?.let { data ->
                        return@withContext Result.success(StudentProfile.fromFirestoreMap(data))
                    }
                }
            }
            Result.success(null)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error searching student by studentId $studentId: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun searchStudents(query: String): Result<List<StudentProfile>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext Result.success(emptyList())

        try {
            if (isFirebaseAvailable) {
                val db = FirebaseFirestore.getInstance()

                // 1. Check if query is a Student ID format
                val normalizedId = StudentIdGenerator.normalizeStudentId(trimmed)
                if (normalizedId != null) {
                    val studentResult = getStudentByStudentId(normalizedId).getOrNull()
                    if (studentResult != null) {
                        return@withContext Result.success(listOf(studentResult))
                    }
                }

                // 2. Search by studentId direct equality
                val idQuery = db.collection("students")
                    .whereEqualTo("studentId", trimmed.uppercase())
                    .limit(5)
                    .get()
                    .awaitTask()
                val idResults = idQuery.documents.mapNotNull { it.data?.let { data -> StudentProfile.fromFirestoreMap(data) } }
                if (idResults.isNotEmpty()) {
                    return@withContext Result.success(idResults)
                }

                // 3. Search by studentName match
                val nameQuery = db.collection("students")
                    .whereEqualTo("studentName", trimmed)
                    .limit(10)
                    .get()
                    .awaitTask()

                val results = nameQuery.documents.mapNotNull { doc ->
                    doc.data?.let { StudentProfile.fromFirestoreMap(it) }
                }

                if (results.isNotEmpty()) {
                    return@withContext Result.success(results)
                }

                // 4. Prefix search on studentName
                val prefixQuery = db.collection("students")
                    .whereGreaterThanOrEqualTo("studentName", trimmed)
                    .whereLessThanOrEqualTo("studentName", "$trimmed\uf8ff")
                    .limit(10)
                    .get()
                    .awaitTask()

                val prefixResults = prefixQuery.documents.mapNotNull { doc ->
                    doc.data?.let { StudentProfile.fromFirestoreMap(it) }
                }

                return@withContext Result.success(prefixResults)
            }
            Result.success(emptyList())
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error searching students in Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun syncStudentProgress(
        userId: String,
        completedTodos: Int,
        streakDays: Int,
        completedChapters: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext Result.success(Unit)
        try {
            if (isFirebaseAvailable) {
                val db = FirebaseFirestore.getInstance()
                val points = (completedTodos * 10) + (streakDays * 15)
                val updates = mapOf<String, Any>(
                    "completedTodoCount" to completedTodos,
                    "studyStreakDays" to streakDays,
                    "competitionPoints" to points,
                    "completedChapterIds" to completedChapters,
                    "updatedAt" to System.currentTimeMillis()
                )
                db.collection("students")
                    .document(userId)
                    .update(updates)
                    .awaitTask()
                Log.d("FirestoreRepo", "Student progress synced to Firestore: todos=$completedTodos, streak=$streakDays, points=$points")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreRepo", "Error updating student progress in Firestore: ${e.message}")
            Result.failure(e)
        }
    }
}
