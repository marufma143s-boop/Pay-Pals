package com.example

import android.app.Application
import android.util.Log
import com.example.data.FirebaseRealtimeDbManager
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class PayPulseApplication : Application() {

    companion object {
        var instance: PayPulseApplication? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(packageName)
                    .setApiKey("AIzaSyBwXf9PayPulseKey918237465")
                    .setDatabaseUrl("https://pay-a9be1-default-rtdb.firebaseio.com")
                    .setProjectId("pay-a9be1")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.i("PayPulseApplication", "FirebaseApp initialized programmatically with options")
            }
            FirebaseRealtimeDbManager.init(this)
        } catch (e: Exception) {
            Log.e("PayPulseApplication", "Failed to initialize Firebase in Application: ${e.message}", e)
        }
    }
}
