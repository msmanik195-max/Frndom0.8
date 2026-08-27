package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class FrndomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                // Try standard initialization
                try {
                    FirebaseApp.initializeApp(this)
                    Log.d("FrndomApplication", "Firebase initialized successfully via default provider.")
                } catch (initErr: Throwable) {
                    Log.w("FrndomApplication", "Default FirebaseApp.initializeApp failed, attempting manual options fallback: ${initErr.message}")
                }

                // If still not initialized, manually build FirebaseOptions from google-services.json credentials
                if (FirebaseApp.getApps(this).isEmpty()) {
                    val apiKey = BuildConfig.FIREBASE_API_KEY.ifBlank { "AIzaSyDbcKf04i6AshXBO0kpmeCNBbkytEo-KwU" }
                    val appId = BuildConfig.FIREBASE_APP_ID.ifBlank { "1:426440213847:android:4f04cda3bbfddf3bb56a12" }
                    val projectId = BuildConfig.FIREBASE_PROJECT_ID.ifBlank { "frndom-e3f3b" }
                    val databaseUrl = BuildConfig.FIREBASE_DATABASE_URL.ifBlank { "https://frndom-e3f3b-default-rtdb.firebaseio.com" }

                    val options = FirebaseOptions.Builder()
                        .setApiKey(apiKey)
                        .setApplicationId(appId)
                        .setProjectId(projectId)
                        .setDatabaseUrl(databaseUrl)
                        .setStorageBucket("frndom-e3f3b.firebasestorage.app")
                        .build()

                    FirebaseApp.initializeApp(this, options)
                    Log.d("FrndomApplication", "Firebase successfully initialized manually with project credentials.")
                }
            }
        } catch (e: Throwable) {
            Log.e("FrndomApplication", "Firebase initialization error: ${e.message}", e)
        }
    }
}

