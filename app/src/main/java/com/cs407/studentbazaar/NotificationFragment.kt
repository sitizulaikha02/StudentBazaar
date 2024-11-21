package com.cs407.studentbazaar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.studentbazaar.data.NotificationItem
import com.cs407.studentbazaar.data.NotificationItemAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationFragment : Fragment() {

    private lateinit var backButton: ImageButton
    private lateinit var notifications: ArrayList<NotificationItem>
    private lateinit var activity: String
    private lateinit var editTextSender: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationItemAdapter

    private val db = FirebaseFirestore.getInstance()
    private val userUid = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        // Initialize buttons
        backButton = view.findViewById(R.id.button_back)

        // Back button click listener
        backButton.setOnClickListener {
            // Navigate back to the previous fragment (or home)
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val followButton: Button = view.findViewById(R.id.followButton)
        val likeButton: Button = view.findViewById(R.id.likeButton)
        editTextSender = view.findViewById(R.id.editTextSender)

        notifications = arrayListOf()
        adapter = NotificationItemAdapter(notifications)

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView4)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Load existing notifications from Firestore
        loadNotificationsFromFirestore()

        followButton.setOnClickListener {
            activity = "started following you."
            handleNotificationAction(activity)
        }

        likeButton.setOnClickListener {
            activity = "liked your item."
            handleNotificationAction(activity)
        }
    }

    private fun loadNotificationsFromFirestore() {
        if (userUid != null) {
            val notificationsRef = db.collection("users")
                .document(userUid)
                .collection("notifications")

            // Query all notifications from Firestore
            notificationsRef.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    notifications.clear() // Clear the existing notifications list
                    for (document in documents) {
                        val activity = document.getString("activity") ?: ""
                        val timestamp = document.getLong("timestamp")?.toInt() ?: 0
                        val iconId = document.getLong("iconId")?.toInt() ?: R.drawable.ic_notifications

                        // Create a NotificationItem and add it to the list
                        notifications.add(
                            NotificationItem(activity, timestamp, iconId)
                        )
                    }
                    adapter.notifyDataSetChanged() // Notify the adapter of data changes
                }
                .addOnFailureListener { e ->
                    Log.e("NotificationFragment", "Error loading notifications: ${e.message}")
                }
        } else {
            Log.e("NotificationFragment", "User not logged in. Cannot load notifications.")
        }
    }

    // Simplified method to handle notifications (reuse for both follow and like actions)
    private fun handleNotificationAction(activity: String) {
        NotificationHelper.getInstance().setNotificationContent(
            editTextSender.text.toString(),
            activity
        )
        NotificationHelper.getInstance().showNotification(requireContext())
        addNotificationToFirestore()
    }

    private fun addNotificationToFirestore() {
        val sender = editTextSender.text.toString()

        if (sender.isNotEmpty() && activity.isNotEmpty()) {
            // Create a new notification
            val newNotification = NotificationItem(
                "$sender $activity",
                0,
                R.drawable.ic_notifications
            )

            notifications.add(0, newNotification)
            adapter.notifyItemInserted(0)
            //recyclerView.adapter = NotificationItemAdapter(notifications)

            // Clear input fields
            editTextSender.text.clear()

            // Scroll to the top
            recyclerView.scrollToPosition(0)

            if (userUid != null) {
                // Reference to the 'notifications' sub-collection for the current user
                val notificationsRef = db.collection("users")
                    .document(userUid)
                    .collection("notifications")

                // Create a Firestore-compatible data map
                val notificationData = hashMapOf(
                    "activity" to "$sender $activity",
                    "timestamp" to System.currentTimeMillis(), // Save the current timestamp
                    "iconId" to R.drawable.ic_notifications // Save the icon resource ID
                )

                // Add the notification document to the sub-collection
                notificationsRef.add(notificationData)
                    .addOnSuccessListener {
                        // Handle success (optional)
                        Log.d("NotificationFragment", "Notification added to Firestore")
                    }
                    .addOnFailureListener { e ->
                        // Handle failure
                        Log.e("NotificationFragment", "Error adding notification to Firestore: ${e.message}")
                    }
            } else {
                Log.e("NotificationFragment", "User not logged in. Cannot save notification to Firestore.")
            }
        }
    }

}