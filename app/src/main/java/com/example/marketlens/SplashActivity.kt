package com.example.marketlens

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val sessionDuration = 3 * 24 * 60 * 60 * 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setContentView
        val prefs = getSharedPreferences("MarketLensPrefs", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("darkMode", true)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            val user = auth.currentUser
            if (user != null && isSessionValid()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                auth.signOut()
                startActivity(Intent(this, SignInActivity::class.java))
            }
            finish()
        }, 2000)
    }

    private fun isSessionValid(): Boolean {
        val prefs = getSharedPreferences("MarketLensPrefs", MODE_PRIVATE)
        val lastLogin = prefs.getLong("lastLoginTime", 0)
        return (System.currentTimeMillis() - lastLogin) < sessionDuration
    }
}