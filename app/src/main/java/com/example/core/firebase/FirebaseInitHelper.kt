package com.example.core.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * Robust Firebase initialization helper.
 *
 * Guarantees that FirebaseApp is initialized before any Auth, Firestore,
 * or Firebase operations occur across debug and release builds.
 */
object FirebaseInitHelper {
    private const val TAG = "FirebaseInitHelper"

    // Project configuration metadata matching google-services.json
    private const val APPLICATION_ID = "1:340392504744:android:d7f7f063838dfa6f5a819d"
    private const val PROJECT_ID = "nexora-learn-2f4a4"
    private const val API_KEY = "AIzaSyB7P4-cKw6XupCjGraGu7qr-hXwhgNeP3k"
    private const val GCM_SENDER_ID = "340392504744"
    private const val STORAGE_BUCKET = "nexora-learn-2f4a4.firebasestorage.app"
    private const val DATABASE_URL = "https://nexora-learn-2f4a4-default-rtdb.firebaseio.com"

    @Volatile
    private var isInitialized = false

    @Synchronized
    fun ensureInitialized(context: Context?): Boolean {
        if (context == null) {
            return isFirebaseReady()
        }

        val appContext = context.applicationContext ?: context

        try {
            if (FirebaseApp.getApps(appContext).isNotEmpty()) {
                isInitialized = true
                return true
            }
        } catch (ignored: Throwable) {}

        // Strategy 1: Attempt standard initialization using google-services.json generated resource mapping
        try {
            val app = FirebaseApp.initializeApp(appContext)
            if (app != null) {
                Log.d(TAG, "Firebase initialized via default resource configuration.")
                isInitialized = true
                return true
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Default resource initialization attempted: ${e.message}")
        }

        // Strategy 2: Explicit fallback initialization using programmatic FirebaseOptions
        return try {
            val options = FirebaseOptions.Builder()
                .setApplicationId(APPLICATION_ID)
                .setProjectId(PROJECT_ID)
                .setApiKey(API_KEY)
                .setGcmSenderId(GCM_SENDER_ID)
                .setStorageBucket(STORAGE_BUCKET)
                .setDatabaseUrl(DATABASE_URL)
                .build()

            val app = FirebaseApp.initializeApp(appContext, options)
            val success = app != null || FirebaseApp.getApps(appContext).isNotEmpty()
            if (success) {
                Log.d(TAG, "Firebase successfully initialized via programmatic FirebaseOptions fallback.")
                isInitialized = true
            }
            success
        } catch (e: Throwable) {
            Log.e(TAG, "Firebase initialization error: ${e.message}", e)
            val ready = isFirebaseReady()
            isInitialized = ready
            ready
        }
    }

    fun isFirebaseReady(): Boolean {
        return try {
            FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()
        } catch (e: Throwable) {
            try {
                FirebaseApp.getApps(null as? Context ?: return false).isNotEmpty()
            } catch (t: Throwable) {
                false
            }
        }
    }
}
