package com.cs407.studentbazaar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.studentbazaar.data.UserItem
import com.cs407.studentbazaar.data.UserItemAdapter

class UserProfileActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemList: ArrayList<UserItem>
    private lateinit var itemAdapter: UserItemAdapter
    private lateinit var editProfileButton: Button
    private lateinit var nameTextView: TextView // TextView for displaying the name
    private lateinit var usernameTextView: TextView // TextView for displaying the username
    private lateinit var bioTextView: TextView // TextView for displaying the bio

    // Register the ActivityResultLauncher
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Get the returned data
            val data = result.data
            val name = data?.getStringExtra("NAME")
            val username = data?.getStringExtra("USERNAME")
            val bio = data?.getStringExtra("BIO")

            // Update the TextViews with the new values
            nameTextView.text = name
            usernameTextView.text = getString(R.string.username_format, username)
            bioTextView.text = bio
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)

        init()

        editProfileButton = findViewById(R.id.editProfileButton)

        editProfileButton.setOnClickListener {
            // Start the EditProfileActivity when the edit profile button is clicked
            val intent = Intent(this, EditProfileActivity::class.java)
            editProfileLauncher.launch(intent) // Launch with the new API
        }

    }

    private fun init() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        itemList = ArrayList()
        addDataToList()

        itemAdapter = UserItemAdapter(itemList)
        recyclerView.adapter = itemAdapter

        nameTextView = findViewById(R.id.displayName) // Initialize your TextView
        usernameTextView = findViewById(R.id.username) // For username
        bioTextView = findViewById(R.id.bio) // For bio
    }

    private fun addDataToList() {
        // Array of drawable resource IDs
        val itemDrawables = intArrayOf(
            R.drawable.tv,
            R.drawable.fan,
            R.drawable.book,
            R.drawable.desk,
            R.drawable.mirror,
            R.drawable.winter_puffer
        )

        // Loop through the array and add each item to the list
        for (i in 0 until 3) { // DUPLICATE IMAGES AS PLACEHOLDER
            for (drawable in itemDrawables) {
                itemList.add(UserItem(drawable))
            }
        }
    }
}