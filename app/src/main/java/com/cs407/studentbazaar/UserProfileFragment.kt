package com.cs407.studentbazaar

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginStart
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.studentbazaar.adapters.UserItem
import com.cs407.studentbazaar.adapters.UserItemAdapter
import com.cs407.studentbazaar.notifications.NotificationSender
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.toString

class UserProfileFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemList: ArrayList<UserItem>
    private lateinit var editProfileButton: Button
    private lateinit var followButton: Button
    private lateinit var logoutButton: ImageView
    private lateinit var backButton: ImageView
    private lateinit var nameTextView: TextView // TextView for displaying the name
    private lateinit var usernameTextView: TextView // TextView for displaying the username
    private lateinit var bioTextView: TextView // TextView for displaying the bio
    private lateinit var listingTextView: TextView
    private lateinit var followersTextView: TextView
    private lateinit var followingTextView: TextView
    private lateinit var notificationButton: ImageView
    private var currentUserUid: String? = null
    private var receiverUid: String? = null

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

        Log.d("UserProfileNavigate", "current: $currentUserUid, receiver: $receiverUid")

        // Set click listener to open EditProfileFragment
        // KIV
        backButton.setOnClickListener {

            if (currentUserUid != receiverUid) {
                findNavController().navigate(R.id.action_userProfileFragment_to_HomepageFragment)
            } else {
                findNavController().navigate(R.id.action_userProfileFragment_to_inboxFragment)
            }
        }

        // Set OnClickListener on the button
        notificationButton.setOnClickListener {
            // Use Navigation Component to navigate to UserProfileFragment
            findNavController().navigate(R.id.action_userProfileFragment_to_notificationFragment)
        }

        return view

    }

    private fun init(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        itemList = ArrayList()
//        fetchItems()

        nameTextView = view.findViewById(R.id.displayName) // Initialize your TextView
        usernameTextView = view.findViewById(R.id.username) // For username
        bioTextView = view.findViewById(R.id.bio) // For bio

        editProfileButton = view.findViewById(R.id.editProfileButton)
        followButton = view.findViewById(R.id.followButton)
        logoutButton = view.findViewById(R.id.logoutButton)
        backButton = view.findViewById(R.id.button_back)
        notificationButton = view.findViewById(R.id.notificationButton)

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
        // Retrieve the receiverUid from arguments
        receiverUid = arguments?.getString("uid")

        // Get the current user's UID
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Check if visiting own profile or another user's profile
        if (receiverUid == null || receiverUid == currentUserUid) {
            // Visiting own profile
            editProfileButton.visibility = View.VISIBLE
            followButton.visibility = View.GONE
            logoutButton.visibility = View.VISIBLE
        } else {
            // Visiting another user's profile
            val marginInDp = -45 // Replace with desired margin in dp
            val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()

            val layoutParams = nameTextView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.marginStart = marginInPx

            nameTextView.layoutParams = layoutParams

            editProfileButton.visibility = View.GONE
            notificationButton.visibility = View.GONE
            followButton.visibility = View.VISIBLE
            logoutButton.visibility = View.GONE

            observeFollowState(receiverUid) // Start observing and handling follow state dynamically
        }

        // Firestore instance
        val db = FirebaseFirestore.getInstance()

        // Reference to the 'users' collection, with the userId as the document ID
        val userDocRef = db.collection("users").document(receiverUid ?: currentUserUid ?: return)

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

                // Update ViewModel or UI directly
                sharedViewModel.setName(name)
                sharedViewModel.setUsername(username)
                sharedViewModel.setBio(bio)

                listingTextView.text = getString(R.string.listing_count, "$listing")
                followersTextView.text = getString(R.string.listing_count, "$followers")
                followingTextView.text = getString(R.string.listing_count, "$following")
            } else {
                Log.d("UserProfileFragment", "User document not found")
            }
        }.addOnFailureListener { e ->
            Log.e("UserProfileFragment", "Failed to load profile data: ${e.message}")
        }

        // Fetch the items for this user
        fetchItems(receiverUid ?: currentUserUid!!)
    }

    // FOLLOWING USER KIV
    private fun observeFollowState(receiverUid: String?) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

//        // Listen for changes in the "following" collection
//        db.collection("users").document(currentUserUid).collection("following").document(receiverUid)
//            .addSnapshotListener { documentSnapshot, error ->
//                if (error != null) {
//                    Log.e("FollowState", "Error observing follow state: ${error.message}")
//                    return@addSnapshotListener
//                }
//
//                if (documentSnapshot != null && documentSnapshot.exists()) {
//                    // User is already following
//                    Log.d("FollowState", "User is following: $receiverUid")
//                    followButton.text = getString(R.string.following)
//                    followButton.setOnClickListener {
//                        unfollowUser(receiverUid) // Trigger unfollow action
//                    }
//                } else {
//                    // User is not following
//                    Log.d("FollowState", "User is not following: $receiverUid")
//                    followButton.text = getString(R.string.follow)
//                    followButton.setOnClickListener {
//                        followUser(receiverUid) // Trigger follow action
//                    }
//                }
//            }
    }

    private fun followUser(receiverUid: String) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // Add receiverUid to current user's "following" list
        db.collection("users").document(currentUserUid).collection("following").document(receiverUid)
            .set(mapOf("timestamp" to System.currentTimeMillis()))
            .addOnSuccessListener {
                Log.d("FollowUser", "Successfully followed user: $receiverUid")
                followButton.text = getString(R.string.following)
            }
            .addOnFailureListener { e ->
                Log.e("FollowUser", "Failed to follow user: ${e.message}")
            }

        // Add currentUserUid to receiver's "followers" list
        db.collection("users").document(receiverUid).collection("followers").document(currentUserUid)
            .set(mapOf("timestamp" to System.currentTimeMillis()))
            .addOnSuccessListener {
                Log.d("FollowUser", "Successfully added user to following list: $receiverUid")
            }
            .addOnFailureListener { e ->
                Log.e("FollowUser", "Failed to add user to following list: ${e.message}")
            }

        // SEND NOTIFICATION
        // Fetch recipient's FCM token
        db.collection("users").document(receiverUid.toString()).get()
            .addOnSuccessListener { document ->
                val fcmToken = document.getString("fcmToken")
                val sharedPref = requireContext().getSharedPreferences("WhoAmI", Context.MODE_PRIVATE)
                val username = sharedPref.getString("USERNAME", "null").toString() // Default value is null if not found
                val notificationMessage = getString(R.string.following_notify, username)
                val title = getString(R.string.new_follower)

                if (!fcmToken.isNullOrEmpty()) {
                    val notificationSender = NotificationSender(requireContext())
                    val notificationType = "following"

                    // Send notification to the recipient
                    notificationSender.sendNotificationToUser(fcmToken, title, notificationMessage, currentUserUid, notificationType)
                } else {
                    Log.e("SendingNotification", "FCM token is missing for user: $receiverUid")
                }
            }
            .addOnFailureListener { e ->
                Log.e("SendingNotification", "Error fetching user token: ${e.message}")
            }
    }

    private fun unfollowUser(receiverUid: String) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // Remove receiverUid from current user's "following" list
        db.collection("users").document(currentUserUid).collection("following").document(receiverUid)
            .delete()
            .addOnSuccessListener {
                Log.d("UnfollowUser", "Successfully unfollowed user: $receiverUid")
                followButton.text = getString(R.string.follow)
            }
            .addOnFailureListener { e ->
                Log.e("UnfollowUser", "Failed to unfollow user: ${e.message}")
            }

        // Remove currentUserUid from receiver's "followers" list
        db.collection("users").document(receiverUid).collection("followers").document(currentUserUid)
            .delete()
    }


    private fun fetchItems(userId: String) {
        // Query Firestore to get items uploaded by this user
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("items")
            .whereEqualTo("userId", userId) // Filter by userId
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val imageUris = mutableListOf<String>() // List to store imageUri values

                for (document in querySnapshot.documents) {
                    // Directly extract the imageUri field from the document
                    val imageUri = document.getString("imageUri")
                    if (!imageUri.isNullOrEmpty()) {
                        imageUris.add(imageUri) // Add to the list if not null or empty
                    }
                }

                // Use the list of imageUris as needed
                // Log.d("ImageUris", "Fetched image URIs: $imageUris")

                // Example: Pass the imageUris to an adapter
                val itemAdapter = UserItemAdapter(imageUris)
                recyclerView.adapter = itemAdapter

            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load user items: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("FetchUserItems", "Error fetching user items", e)
            }

    }


}


