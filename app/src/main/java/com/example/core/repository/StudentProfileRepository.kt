package com.example.core.repository

import android.content.Context
import android.util.Log
import com.example.core.model.StudentProfile
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
        get() = try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Throwable) {
            false
        }

    override suspend fun saveProfile(profile: StudentProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (isFirebaseAvailable) {
                val db = FirebaseFirestore.getInstance()
                db.collection("students")
                    .document(profile.userId)
                    .set(profile.toFirestoreMap(), SetOptions.merge())
                    .awaitTask()
                Log.d("FirestoreRepo", "Student profile saved in Firestore for userId: ${profile.userId}")
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
            if (isFirebaseAvailable) {
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("students")
                    .document(userId)
                    .get()
                    .awaitTask()

                if (snapshot.exists()) {
                    val data = snapshot.data
                    if (data != null) {
                        return@withContext Result.success(StudentProfile.fromFirestoreMap(data))
                    }
                }
            }
            Result.success(null)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error getting profile from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun searchStudents(query: String): Result<List<StudentProfile>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext Result.success(emptyList())

        try {
            if (isFirebaseAvailable) {
                val db = FirebaseFirestore.getInstance()
                // Search by direct userId match first
                val directDoc = db.collection("students").document(trimmed).get().awaitTask()
                if (directDoc.exists()) {
                    directDoc.data?.let { data ->
                        return@withContext Result.success(listOf(StudentProfile.fromFirestoreMap(data)))
                    }
                }

                // Search by studentName match
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

                // Prefix search
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
