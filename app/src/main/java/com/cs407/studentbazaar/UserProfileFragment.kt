package com.cs407.studentbazaar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.studentbazaar.EditProfileFragment
import com.cs407.studentbazaar.data.UserItem
import com.cs407.studentbazaar.data.UserItemAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    private lateinit var listingTextView: TextView
    private lateinit var followersTextView: TextView
    private lateinit var followingTextView: TextView
    private lateinit var notificationButton: ImageView

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        // Initialize the UI components
        init(view)

        // Observe data in ViewModel and update UI
        observeViewModel()

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

        // Initialize the button
        notificationButton = view.findViewById(R.id.notificationButton)
        // Set OnClickListener on the button
        notificationButton.setOnClickListener {
            // Use Navigation Component to navigate to UserProfileFragment
            findNavController().navigate(R.id.action_userProfileFragment_to_notificationFragment)
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

        listingTextView = view.findViewById(R.id.listingCount)
        followersTextView = view.findViewById(R.id.followersCount)
        followingTextView = view.findViewById(R.id.followingCount)
    }

    private fun observeViewModel() {
        sharedViewModel.name.observe(viewLifecycleOwner) { name ->
            nameTextView.text = name
        }

        sharedViewModel.username.observe(viewLifecycleOwner) { username ->
            usernameTextView.text = getString(R.string.username_format, username)
        }

        sharedViewModel.bio.observe(viewLifecycleOwner) { bio ->
            bioTextView.text = bio
        }
    }

    private fun loadProfileData() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        currentUserUid?.let { uid ->
            // Firestore instance
            val db = FirebaseFirestore.getInstance()

            // Reference to the 'users' collection, with the user's UID as the document ID
            val userDocRef = db.collection("users").document(uid)

            // Fetch the user data
            userDocRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Retrieve user data
                    val name = documentSnapshot.getString("name") ?: "Default Name"
                    val username = documentSnapshot.getString("username") ?: "Default Username"
                    val bio = documentSnapshot.getString("bio") ?: "Default Bio"
                    val listing: Long = documentSnapshot.getLong("listing") ?: 0
                    val followers = documentSnapshot.getLong("followers") ?: 0
                    val following = documentSnapshot.getLong("following") ?: 0

                    // Update ViewModel
                    sharedViewModel.setName(name)
                    sharedViewModel.setUsername(username)
                    sharedViewModel.setBio(bio)

                    listingTextView.text = getString(R.string.listing_count, "$listing")
                    followersTextView.text = getString(R.string.listing_count, "$followers")
                    followingTextView.text = getString(R.string.listing_count, "$following")
                } else {
                    // Handle case where the document doesn't exist
                    Log.d("loadProfileData", "Successfully loaded profile data")
                }
            }.addOnFailureListener { e ->
                // Handle error
                Log.d("loadProfileData", "Failed to load profile data: ${e.message}")
            }
        } ?: run {
            // If no user is logged in
            Log.d("loadProfileData", "No user is logged in")
        }

//        // ========================================
//        // Retrieve the email from sharedPreferences
//        val sharedPref = requireContext().getSharedPreferences("WhoAmI", Context.MODE_PRIVATE)
//        val email = sharedPref.getString("EMAIL", null)
//
//        email?.let { currentUserEmail ->
//            // Launch a coroutine to load the user data from the database
//            lifecycleScope.launch {
//                val userData = getUserFromDatabase(currentUserEmail)
//                userData.let {
//                    // Update the TextViews with the retrieved values
//                    nameTextView.text = it.name
//                    usernameTextView.text = getString(R.string.username_format, it.username)
//                    bioTextView.text = it.bio
//                }
//            }
//        }
    }

//    private suspend fun getUserFromDatabase(userEmail: String): User {
//        return withContext(Dispatchers.IO) {
//            database.userDao().getByEmail(userEmail)
//        }
//    }

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


