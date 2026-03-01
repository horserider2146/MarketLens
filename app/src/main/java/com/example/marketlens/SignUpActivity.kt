package com.example.marketlens

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameInput = findViewById<TextInputEditText>(R.id.nameInput)
        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val signUpBtn = findViewById<Button>(R.id.signUpBtn)
        val signInLink = findViewById<TextView>(R.id.signInLink)

        signUpBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signUpBtn.isEnabled = false
            signUpBtn.text = "Creating account..."

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        val userMap = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "createdAt" to System.currentTimeMillis()
                        )

                        // Save to Firestore but don't wait — navigate immediately
                        db.collection("users").document(userId).set(userMap)

                        saveLoginTime()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        signUpBtn.isEnabled = true
                        signUpBtn.text = "Create Account"
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        signInLink.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun saveLoginTime() {
        val prefs = getSharedPreferences("MarketLensPrefs", MODE_PRIVATE)
        prefs.edit().putLong("lastLoginTime", System.currentTimeMillis()).apply()
    }
}