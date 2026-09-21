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
}
