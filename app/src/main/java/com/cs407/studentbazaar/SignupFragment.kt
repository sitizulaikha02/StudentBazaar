package com.cs407.studentbazaar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cs407.studentbazaar.data.AppDatabase
import com.cs407.studentbazaar.data.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignupFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // Initialize the Room database
        database = AppDatabase.getDatabase(requireContext())


        // Get references to UI components
        val usernameEditText = view.findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = view.findViewById<EditText>(R.id.confirm_password)
        val signupButton = view.findViewById<Button>(R.id.signupButton)
        val loginRedirectButton = view.findViewById<Button>(R.id.LoginRedirect)

        // Handle Sign-Up
        signupButton.setOnClickListener {
            val email = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (password == confirmPassword) {
                registerUser(email, password)
            } else {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Login Redirect
        loginRedirectButton.setOnClickListener {
            // Navigate to LoginFragment
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }

        return view
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Registration successful.", Toast.LENGTH_SHORT).show()

                    // Get other user information, such as `displayName` and `bio` if available
                    val username = email.substringBefore("@") // Just an example to generate a username

                    // Insert user info into Room database
                    lifecycleScope.launch {
                        saveUserToDatabase(email, username)

                        // Store username in SharedPreferences
                        val sharedPref = requireContext().getSharedPreferences("WhoAmI", Context.MODE_PRIVATE)
                        with (sharedPref.edit()) {
                            putString("EMAIL", email)
                            apply()
                        }
                    }

                    // Navigate to UserPreferencesFragment after successful registration
                    findNavController().navigate(R.id.action_signupFragment_to_userPreferencesFragment)
                } else {
                    Toast.makeText(requireContext(), "Registration failed. ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to save user to Room database
    private suspend fun saveUserToDatabase(email: String, username: String) {
        withContext(Dispatchers.IO) {
            val userDao = database.userDao()
            val user = User(
                email = email,
                username = username,
                name = "Default Name",
                bio = "Default Bio"
            )
            userDao.insert(user)
        }
    }
}