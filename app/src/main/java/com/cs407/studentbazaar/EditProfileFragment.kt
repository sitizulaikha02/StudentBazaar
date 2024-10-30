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

class EditProfileFragment : Fragment() {

    private lateinit var backButton: ImageView
    private lateinit var saveButton: Button
    private lateinit var editName: EditText
    private lateinit var editUsername: EditText
    private lateinit var editBio: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

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
        val sharedPref = requireContext().getSharedPreferences("UserProfileData", Context.MODE_PRIVATE)
        editName.setText(sharedPref.getString("NAME", ""))
        editUsername.setText(sharedPref.getString("USERNAME", ""))
        editBio.setText(sharedPref.getString("BIO", ""))
    }

    private fun saveProfileData() {
        val name = editName.text.toString()
        val username = editUsername.text.toString()
        val bio = editBio.text.toString()

        val sharedPref = requireContext().getSharedPreferences("UserProfileData", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("NAME", name)
            putString("USERNAME", username)
            putString("BIO", bio)
            apply()
        }
    }
}