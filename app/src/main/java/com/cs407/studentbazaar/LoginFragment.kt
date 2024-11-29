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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Get references to UI components
        val usernameEditText = view.findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val loginButton = view.findViewById<Button>(R.id.signupButton) // Assuming login button is defined with ID `signupButton` in XML
        val signUpRedirectButton = view.findViewById<Button>(R.id.signupRedirect)

        // Login Button Click - To Sign In User
        loginButton.setOnClickListener {
            val email = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Check if the email and password fields are empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Sign-in successful.", Toast.LENGTH_SHORT).show()

                        // Save both email and userId to sharedpref
                        FirebaseAuth.getInstance().currentUser?.let { user ->
                            val userId = user.uid
                            val sharedPref = requireContext().getSharedPreferences("WhoAmI", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("EMAIL", email) // Replace email with the actual email value
                                putString("USER_ID", userId)
                                apply()
                            }

                        }

                        // Navigate to HomePageFragment after successful login
                        findNavController().navigate(R.id.action_loginFragment_to_homepageFragment)
                    } else {
                        Toast.makeText(requireContext(), "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
        }



        // Sign Up Button Click - To Redirect to Sign-Up Screen
        signUpRedirectButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        return view
    }

}