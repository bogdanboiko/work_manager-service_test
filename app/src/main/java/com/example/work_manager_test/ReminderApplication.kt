package com.example.work_manager_test

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ReminderApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        appContext = this.applicationContext
        Log.e("app", "app created")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.e("app", "app terminated on low memory")
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.e("app", "app terminated")
    }

    companion object {
        private lateinit var appContext: Context
    }
}