package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class NexoraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            Log.d("NexoraApplication", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.w("NexoraApplication", "Firebase initialization deferred: ${e.message}")
        }
    }
}
