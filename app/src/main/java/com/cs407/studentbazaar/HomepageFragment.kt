package com.cs407.studentbazaar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.studentbazaar.adapters.PublishedItemAdapter
import com.cs407.studentbazaar.data.PublishedItem
import com.google.firebase.firestore.FirebaseFirestore

class HomepageFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PublishedItemAdapter
    private val items = mutableListOf<PublishedItem>()
    private val firestore = FirebaseFirestore.getInstance()

    // Add the buttons for navigation
    private lateinit var usernameButton: ImageButton
    private lateinit var publishItemButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_homepage, container, false)

        // Initialize RecyclerView and set the adapter
        recyclerView = view.findViewById(R.id.recyclerView2)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PublishedItemAdapter(items)
        recyclerView.adapter = adapter

        // Fetch and display the items from Firestore
        fetchItems()

        // Initialize the buttons for navigation
        usernameButton = view.findViewById(R.id.usernameButton)
        publishItemButton = view.findViewById(R.id.publish_item)

        // Set OnClickListener for navigating to UserProfileFragment
        usernameButton.setOnClickListener {
            findNavController().navigate(R.id.action_homepageFragment_to_userProfileFragment)
        }

        // Set OnClickListener for navigating to PublishItemFragment
        publishItemButton.setOnClickListener {
            findNavController().navigate(R.id.action_homepageFragment_to_publishItemFragment)
        }

        return view
    }

    private fun fetchItems() {
        // Fetch all items from Firestore collection 'items'
        firestore.collection("items")
            .get()  // Get the data from Firestore
            .addOnSuccessListener { querySnapshot ->
                items.clear() // Clear current items in the list
                for (document in querySnapshot.documents) {
                    val item = document.toObject(PublishedItem::class.java)
                    if (item != null) {
                        items.add(item) // Add the item to the list
                    }
                }
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                // Show error message if data fetch fails
                Toast.makeText(requireContext(), "Failed to load items: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
