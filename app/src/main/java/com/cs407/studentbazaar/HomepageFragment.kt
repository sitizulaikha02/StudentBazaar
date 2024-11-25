package com.cs407.studentbazaar

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import kotlin.getValue
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.cs407.studentbazaar.adapters.PublishedItemAdapter
import com.cs407.studentbazaar.data.PublishedItem
import com.google.firebase.firestore.FirebaseFirestore

class HomepageFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PublishedItemAdapter
    private val items = mutableListOf<PublishedItem>()
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var usernameButton: ImageButton
    private lateinit var publishItemButton: Button
    private lateinit var searchBox: SearchView
    private lateinit var newSwitch: Switch
    private lateinit var nearbySwitch: Switch
    private lateinit var priceLowSwitch: Switch
    private lateinit var priceHighSwitch: Switch
    private lateinit var cartButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_homepage, container, false)

        // Request notifications permission
        requestPermission()
        NotificationHelper.getInstance().createNotificationChannel(requireContext())

        // Initialize the button
        // Initialize RecyclerView and set the adapter
        recyclerView = view.findViewById(R.id.recyclerView2)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PublishedItemAdapter(items.toMutableList())
        recyclerView.adapter = adapter

        // Initialize buttons and switches
        usernameButton = view.findViewById(R.id.usernameButton)
        publishItemButton = view.findViewById(R.id.publish_item)
        searchBox = view.findViewById<SearchView>(R.id.searchBox)
        newSwitch = view.findViewById(R.id.newSwitch)
        nearbySwitch = view.findViewById(R.id.nearbySwitch)
        priceLowSwitch = view.findViewById(R.id.priceLowSwitch)
        priceHighSwitch = view.findViewById(R.id.priceHighSwitch)
        cartButton = view.findViewById(R.id.cartButton)

        // Fetch and display the items from Firestore
        fetchItems()

        // Set button listeners
        usernameButton.setOnClickListener {
            findNavController().navigate(R.id.action_homepageFragment_to_userProfileFragment)
        }

        publishItemButton.setOnClickListener {
            findNavController().navigate(R.id.action_homepageFragment_to_publishItemFragment)
        }

        cartButton.setOnClickListener {
            findNavController().navigate(R.id.action_HomepageFragment_to_cartFragment)
        }

        // Set search and filter listeners
        setSearchAndFilterListeners()

        return view
    }

    // ********************************
    // REQUEST NOTIFICATIONS PERMISSION
    // ********************************
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(
                requireContext(),
                "Please allow all notifications to continue.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @VisibleForTesting
    public fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }


    private fun fetchItems() {
        firestore.collection("items")
            .get()
            .addOnSuccessListener { querySnapshot ->
                items.clear()
                for (document in querySnapshot.documents) {
                    val item = document.toObject(PublishedItem::class.java)
                    if (item != null) {
                        Log.d("FetchItems", "Fetched item: ${item.title}, imageUri: ${item.imageUri}")
                        items.add(item)
                    }
                }
                adapter.updateItems(items) // Initial display of all items
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load items: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setSearchAndFilterListeners() {
        // SearchView Listener
        searchBox.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterAndSearchItems(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterAndSearchItems(newText.orEmpty())
                return true
            }
        })

        // Switch Listeners
        newSwitch.setOnCheckedChangeListener { _, _ -> filterAndSearchItems(searchBox.query.toString()) }
        nearbySwitch.setOnCheckedChangeListener { _, _ -> filterAndSearchItems(searchBox.query.toString()) }
        priceLowSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                priceHighSwitch.isChecked = false // Disable the other switch
            }
            filterAndSearchItems(searchBox.query.toString())
        }
        priceHighSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                priceLowSwitch.isChecked = false // Disable the other switch
            }
            filterAndSearchItems(searchBox.query.toString())
        }
    }

    private fun filterAndSearchItems(query: String = "") {
        // Filter items based on the search query and switches
        val filteredItems = items.filter { item ->
            val matchesQuery = item.title.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
            val matchesNew = !newSwitch.isChecked || item.label.equals("new", ignoreCase = true)
            val matchesNearby = !nearbySwitch.isChecked || isNearby(item) // Replace with real nearby logic

            matchesQuery && matchesNew && matchesNearby
        }

        // Sort items based on the price filters
        val sortedItems = when {
            priceLowSwitch.isChecked -> filteredItems.sortedBy { it.price } // Low to high
            priceHighSwitch.isChecked -> filteredItems.sortedByDescending { it.price } // High to low
            else -> filteredItems // No sorting applied
        }

        // Update RecyclerView with the filtered and sorted items
        adapter.updateItems(sortedItems)
    }

    private fun isNearby(item: PublishedItem): Boolean {
        // Placeholder for "nearby" logic
        return true // Replace with real location checking
    }
}

