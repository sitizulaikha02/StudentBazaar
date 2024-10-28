package com.cs407.studentbazaar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {
    private lateinit var backButton: ImageView
    private lateinit var saveButton: Button
    private lateinit var editName: EditText
    private lateinit var editUsername: EditText
    private lateinit var editBio: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        backButton = findViewById(R.id.backButton)
        saveButton = findViewById(R.id.saveButton)
        editName = findViewById(R.id.editName)
        editUsername = findViewById(R.id.editUsername)
        editBio = findViewById(R.id.editBio)


        // Set the click listener
        backButton.setOnClickListener {
            // Navigate back to UserProfileActivity
            finish() // This will close the EditProfileActivity and return to the previous activity
        }

        saveButton.setOnClickListener {
            // Get the input values
            val name = editName.text.toString()
            val username = editUsername.text.toString()
            val bio = editBio.text.toString()

            // Create an intent to hold the result
            val resultIntent = Intent().apply {
                putExtra("NAME", name)
                putExtra("USERNAME", username)
                putExtra("BIO", bio)
            }
            setResult(RESULT_OK, resultIntent) // Return the result
            finish() // Close the activity
        }
    }
}