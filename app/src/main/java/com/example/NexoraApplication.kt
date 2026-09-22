package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.core.firebase.FirebaseInitHelper

class NexoraApplication : Application() {

    companion object {
        lateinit var instance: NexoraApplication
            private set

        val appContext: Context
            get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            val initialized = FirebaseInitHelper.ensureInitialized(this)
            Log.d("NexoraApplication", "Firebase initialization complete (success=$initialized)")
        } catch (e: Exception) {
            Log.e("NexoraApplication", "Firebase initialization exception: ${e.message}", e)
        }
    }
}
