package com.cs407.studentbazaar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.cs407.studentbazaar.data.AppDatabase
import com.cs407.studentbazaar.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    private val sharedViewModel: SharedViewModel by activityViewModels()


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

        // Load data from SharedViewModel into EditTexts
        observeViewModel()

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

    private fun observeViewModel() {
        sharedViewModel.name.observe(viewLifecycleOwner) { name ->
            editName.setText(name)
        }

        sharedViewModel.username.observe(viewLifecycleOwner) { username ->
            editUsername.setText(username)
        }

        sharedViewModel.bio.observe(viewLifecycleOwner) { bio ->
            editBio.setText(bio)
        }
    }

    private fun saveProfileData() {
        val updatedName = editName.text.toString()
        val updatedUsername = editUsername.text.toString()
        val updatedBio = editBio.text.toString()

        // Update the SharedViewModel
        sharedViewModel.setName(updatedName)
        sharedViewModel.setUsername(updatedUsername)
        sharedViewModel.setBio(updatedBio)

        // Save to Firestore for persistence
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        currentUserUid?.let { uid ->
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(uid)

            val updatedData = mapOf(
                "name" to updatedName,
                "username" to updatedUsername,
                "bio" to updatedBio
            )

            userDocRef.update(updatedData)
                .addOnSuccessListener {
                    Log.d("EditProfileFragment", "Profile updated successfully in Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e(
                        "EditProfileFragment",
                        "Error updating profile in Firestore: ${e.message}"
                    )
                }
        }
    }
}