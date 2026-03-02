package com.example.marketlens

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MarketLensApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Apply saved theme on app start
        val prefs = getSharedPreferences("MarketLensPrefs", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("darkMode", true)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}