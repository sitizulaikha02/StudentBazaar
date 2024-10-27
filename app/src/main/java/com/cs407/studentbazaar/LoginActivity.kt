package com.cs407.studentbazaar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Get references to UI components
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.signupButton) // This is your login button in XML
        val signUpRedirectButton = findViewById<Button>(R.id.signupRedirect)

        // Login Button Click - To Sign In User
        loginButton.setOnClickListener {
            val email = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Check if the email and password fields are empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Sign-in successful.", Toast.LENGTH_SHORT).show()
                        // Redirect to UserPreferencesActivity after successful login
                        val intent = Intent(this, UserPreferencesActivity::class.java)
                        startActivity(intent)
                        finish() // Optional: Close LoginActivity if you want to prevent going back
                    } else {
                        Toast.makeText(this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Sign Up Button Click - To Redirect to Sign-Up Screen
        signUpRedirectButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
