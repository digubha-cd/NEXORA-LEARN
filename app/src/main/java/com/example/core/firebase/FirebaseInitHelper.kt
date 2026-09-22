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
            val fromResourceOptions = FirebaseOptions.fromResource(appContext)
            if (fromResourceOptions != null) {
                val app = FirebaseApp.initializeApp(appContext, fromResourceOptions)
                if (app != null) {
                    Log.d(TAG, "Firebase initialized via fromResource options (projectId=${app.options.projectId})")
                    isInitialized = true
                    return true
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "fromResource initialization attempt: ${e.message}")
        }

        // Strategy 2: Attempt standard default initializeApp
        try {
            val app = FirebaseApp.initializeApp(appContext)
            if (app != null) {
                Log.d(TAG, "Firebase initialized via default resource configuration (projectId=${app.options.projectId})")
                isInitialized = true
                return true
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Default resource initialization attempted: ${e.message}")
        }

        val ready = isFirebaseReady()
        isInitialized = ready
        return ready
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
