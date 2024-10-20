package com.cs407.studentbazaar

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.SharedPreferences
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest


class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var errorTextView: TextView
    private lateinit var userPasswdKV: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val view = setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        usernameEditText = view.findViewById(R.id.usernameEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        loginButton = view.findViewById(R.id.loginButton)
        errorTextView = view.findViewById(R.id.errorTextView)

        userViewModel = if (injectedUserViewModel != null) {
            injectedUserViewModel
        } else {
            // Use ViewModelProvider to init UserViewModel
            ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        }

        // Get shared preferences from using R.string.userPasswdKV as the name
        val sharedPrefName = getString(R.string.userPasswdKV)
        userPasswdKV = requireContext().getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        usernameEditText.doAfterTextChanged {
            errorTextView.visibility = View.GONE
        }

        passwordEditText.doAfterTextChanged {
            errorTextView.visibility = View.GONE
        }

        // Set the login button click action
        loginButton.setOnClickListener {
            // Get the entered username and password from EditText fields
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Show an error message if either username or password is empty
            if (username.isEmpty() || password.isEmpty()) {
                errorTextView.visibility = View.VISIBLE
            } else {
                CoroutineScope(Dispatchers.Default).launch {
                    val isAunthenticated = getUserPasswd(username, password)
                    withContext(Dispatchers.Main) {
                        if (isAunthenticated) {
                            // Set the logged-in user in the ViewModel (store user info) (placeholder)
                            userViewModel.setUser(
                                UserState(
                                    id = 0,
                                    name = username,
                                    passwd = password
                                )
                            )

                            // Navigate to another fragment after successful login
                            findNavController().navigate(R.id.action_loginFragment_to_noteListFragment)
                        } else {
                            errorTextView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private suspend fun getUserPasswd(
        name: String,
        passwdPlain: String
    ): Boolean {

        // Hash the plain password using a secure hashing function
        val hashPasswd = hash(passwdPlain)

        // Check if the user exists in SharedPreferences (using the username as the key)
        if (userPasswdKV.contains(name)) {
            // Retrieve the stored password from SharedPreferences
            val storedHashPasswd = userPasswdKV.getString(name, "")

            // Compare the hashed password with the stored one and return false if they don't match
            if (hashPasswd == storedHashPasswd) {
                return true   // Return true if the user login is successful
            }
        } else {
            // If the user doesn't exist in SharedPreferences, create a new user
            val editor = userPasswdKV.edit()
            // Store the hashed password in SharedPreferences for future logins
            editor.putString(name, hashPasswd)
            editor.apply()

            // Initialize the database and UserDao
            val userDao = noteDB.userDao()
            // Create a new User object
            val newUser = User(
                userName = name
            )

            // Insert the new user into the Room database (call insert method on UserDao)
            CoroutineScope(Dispatchers.IO).launch {
                userDao.insert(newUser)  // Insert the user in a background thread
            }

            return true   // Return true if the user was newly created
        }
        return false
    }

    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}