package com.example.marketlens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val profileName = view.findViewById<TextView>(R.id.profileName)
        val profileEmail = view.findViewById<TextView>(R.id.profileEmail)
        val profileNameDetail = view.findViewById<TextView>(R.id.profileNameDetail)
        val profileEmailDetail = view.findViewById<TextView>(R.id.profileEmailDetail)
        val signOutBtn = view.findViewById<Button>(R.id.signOutBtn)
        val darkModeSwitch = view.findViewById<SwitchMaterial>(R.id.darkModeSwitch)

        // Load saved theme preference
        val prefs = requireContext().getSharedPreferences("MarketLensPrefs", android.content.Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("darkMode", true)
        darkModeSwitch.isChecked = isDarkMode

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("darkMode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            requireActivity().recreate()
        }

        // Load user data
        val userId = auth.currentUser?.uid
        val email = auth.currentUser?.email ?: ""
        profileEmail.text = email
        profileEmailDetail.text = email

        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name") ?: "User"
                    profileName.text = name
                    profileNameDetail.text = name
                }
        }

        signOutBtn.setOnClickListener {
            prefs.edit().clear().apply()
            auth.signOut()
            startActivity(Intent(requireContext(), SignInActivity::class.java))
            requireActivity().finish()
        }
    }
}