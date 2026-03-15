package com.example.marketlens

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.*
import java.util.concurrent.TimeUnit

class MarketLensApp : Application() {

    override fun onCreate() {
        super.onCreate()
        applyTheme()
        schedulePriceAlertWorker()
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("MarketLensPrefs", MODE_PRIVATE)
        if (!prefs.contains("darkMode")) {
            // First launch — follow system default
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else {
            val isDarkMode = prefs.getBoolean("darkMode", true)
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun schedulePriceAlertWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<PriceAlertWorker>(30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            PriceAlertWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
