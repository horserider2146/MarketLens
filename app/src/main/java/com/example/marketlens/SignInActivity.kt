package com.example.marketlens

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val signInBtn = findViewById<Button>(R.id.signInBtn)
        val signUpLink = findViewById<TextView>(R.id.signUpLink)

        signInBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signInBtn.isEnabled = false
            signInBtn.text = "Signing in..."

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveLoginTime()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        signInBtn.isEnabled = true
                        signInBtn.text = "Sign In"
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        signUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    private fun saveLoginTime() {
        val prefs = getSharedPreferences("MarketLensPrefs", MODE_PRIVATE)
        prefs.edit().putLong("lastLoginTime", System.currentTimeMillis()).apply()
    }
}