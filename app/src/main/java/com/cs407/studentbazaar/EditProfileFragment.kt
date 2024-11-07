package com.cs407.studentbazaar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cs407.studentbazaar.data.AppDatabase
import com.cs407.studentbazaar.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileFragment : Fragment() {

    private lateinit var backButton: ImageView
    private lateinit var saveButton: Button
    private lateinit var editName: EditText
    private lateinit var editUsername: EditText
    private lateinit var editBio: EditText
    private lateinit var database: AppDatabase
    private lateinit var currentUserEmail: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        // Initialize the Room database
        database = AppDatabase.getDatabase(requireContext())

        // Retrieve the username from SharedPreferences (used as identifier)
        val sharedPref = requireContext().getSharedPreferences("WhoAmI", Context.MODE_PRIVATE)
        currentUserEmail = sharedPref.getString("EMAIL", "") ?: ""

        // Initialize UI components
        backButton = view.findViewById(R.id.backButton)
        saveButton = view.findViewById(R.id.saveButton)
        editName = view.findViewById(R.id.editName)
        editUsername = view.findViewById(R.id.editUsername)
        editBio = view.findViewById(R.id.editBio)

        // Load existing data into EditTexts
        loadProfileData()

        // Set click listener for the back button
        backButton.setOnClickListener {
            // Use the activity's onBackPressedDispatcher to navigate back
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        saveButton.setOnClickListener {
            saveProfileData()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return view
    }

    private fun loadProfileData() {
        // Launch a coroutine to fetch data from the Room database
        lifecycleScope.launch {
            val userData = getUserFromDatabase(currentUserEmail)
            userData.let {
                editName.setText(it.name)
                editUsername.setText(it.username)
                editBio.setText(it.bio)
            }
        }
    }

    private suspend fun getUserFromDatabase(userEmail: String): User {
        return withContext(Dispatchers.IO) {
            database.userDao().getByEmail(userEmail)
        }
    }

    private fun saveProfileData() {
        val name = editName.text.toString()
        val username = editUsername.text.toString()
        val bio = editBio.text.toString()

        // Update the database with the new profile information
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                database.userDao().updateProfile(
                    email = currentUserEmail,
                    newUsername = username,
                    name = name,
                    bio = bio)
            }
        }
    }
}