package com.cs407.studentbazaar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class SignupFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

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
                    // Navigate to UserPreferencesFragment after successful registration
                    findNavController().navigate(R.id.action_signupFragment_to_userPreferencesFragment)
                } else {
                    Toast.makeText(requireContext(), "Registration failed. ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}