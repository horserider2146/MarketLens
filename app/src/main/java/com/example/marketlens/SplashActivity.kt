package com.example.marketlens

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    // Auto sign out after 3 days of inactivity
    private val sessionDuration = 3 * 24 * 60 * 60 * 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            val user = auth.currentUser
            if (user != null && isSessionValid()) {
                // Already logged in and session is valid
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // Not logged in or session expired
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