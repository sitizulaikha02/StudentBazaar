package com.cs407.studentbazaar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.studentbazaar.data.AppDatabase
import com.cs407.studentbazaar.data.User
import com.cs407.studentbazaar.data.UserItem
import com.cs407.studentbazaar.data.UserItemAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserProfileFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemList: ArrayList<UserItem>
    private lateinit var itemAdapter: UserItemAdapter
    private lateinit var editProfileButton: Button
    private lateinit var logoutButton: Button
    private lateinit var nameTextView: TextView // TextView for displaying the name
    private lateinit var usernameTextView: TextView // TextView for displaying the username
    private lateinit var bioTextView: TextView // TextView for displaying the bio

    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        // Initialize the UI components
        init(view)

        // Get the Room database instance
        database = AppDatabase.getDatabase(requireContext())

        // Load data from SharedPreferences
        loadProfileData()

        // Set click listener to open EditProfileFragment
        editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_userProfileFragment_to_editProfileFragment)
        }

        // Set click listener to log out
        logoutButton.setOnClickListener {
            // Clear SharedPreferences
            val sharedPref = requireContext().getSharedPreferences("WhoAmI", Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            findNavController().navigate(R.id.action_userProfileFragment_to_loginFragment)
        }

        // Enable edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        return view

    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        setHasOptionsMenu(true)
//        super.onCreate(savedInstanceState)
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
//        inflater!!.inflate(R.menu.user_menu, menu)
//    }


    private fun init(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        itemList = ArrayList()
        addDataToList()

        itemAdapter = UserItemAdapter(itemList)
        recyclerView.adapter = itemAdapter

        nameTextView = view.findViewById(R.id.displayName) // Initialize your TextView
        usernameTextView = view.findViewById(R.id.username) // For username
        bioTextView = view.findViewById(R.id.bio) // For bio

        editProfileButton = view.findViewById(R.id.editProfileButton)
        logoutButton = view.findViewById(R.id.logoutButton)
    }

    private fun loadProfileData() {
        // Retrieve the email from sharedPreferences
        val sharedPref = requireContext().getSharedPreferences("WhoAmI", Context.MODE_PRIVATE)
        val email = sharedPref.getString("EMAIL", null)

        email?.let { currentUserEmail ->
            // Launch a coroutine to load the user data from the database
            lifecycleScope.launch {
                val userData = getUserFromDatabase(currentUserEmail)
                userData.let {
                    // Update the TextViews with the retrieved values
                    nameTextView.text = it.name
                    usernameTextView.text = getString(R.string.username_format, it.username)
                    bioTextView.text = it.bio
                }
            }
        }
    }

    private suspend fun getUserFromDatabase(userEmail: String): User {
        return withContext(Dispatchers.IO) {
            database.userDao().getByEmail(userEmail)
        }
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


