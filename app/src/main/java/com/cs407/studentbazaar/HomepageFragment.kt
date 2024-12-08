package com.cs407.studentbazaar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.cs407.studentbazaar.adapters.PublishedItemAdapter
import com.cs407.studentbazaar.adapters.PublishedItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomepageFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PublishedItemAdapter
    private val items = mutableListOf<PublishedItem>()
    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private lateinit var usernameButton: ImageButton
    private lateinit var publishItemButton: Button
    private lateinit var searchBox: SearchView
    private lateinit var newSwitch: Switch
    private lateinit var nearbySwitch: Switch
    private lateinit var priceLowSwitch: Switch
    private lateinit var priceHighSwitch: Switch
    private lateinit var cartButton: ImageView
    private lateinit var inboxButton: ImageView
    private lateinit var greetingBox: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_homepage, container, false)

        // Initialize UI elements
        recyclerView = view.findViewById(R.id.recyclerView2)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PublishedItemAdapter(items.toMutableList()) { selectedItem ->
            // Navigate to ViewItemFragment and pass the selected item's data
            val bundle = Bundle().apply {
                putString("id", selectedItem.id)
                putString("title", selectedItem.title)
                putString("description", selectedItem.description)
                putString("imageUri", selectedItem.imageUri)
                putDouble("price", selectedItem.price)
                putString("label", selectedItem.label)
                putString("condition", selectedItem.condition)
                putString("userId", selectedItem.userId)
            }
            findNavController().navigate(R.id.action_homepageFragment_to_viewItemFragment, bundle)
        }
        recyclerView.adapter = adapter

        usernameButton = view.findViewById(R.id.usernameButton)
        publishItemButton = view.findViewById(R.id.publish_item)
        searchBox = view.findViewById<SearchView>(R.id.searchBox)
        newSwitch = view.findViewById(R.id.newSwitch)
        nearbySwitch = view.findViewById(R.id.nearbySwitch)
        priceLowSwitch = view.findViewById(R.id.priceLowSwitch)
        priceHighSwitch = view.findViewById(R.id.priceHighSwitch)
        cartButton = view.findViewById(R.id.cartButton)
        inboxButton = view.findViewById(R.id.inboxButton)
        greetingBox = view.findViewById(R.id.greetingHome)

        // Load user details and items
        loadUserData()
        fetchItems()

        // Set button listeners
        usernameButton.setOnClickListener {
            findNavController().navigate(R.id.action_homepageFragment_to_userProfileFragment)
        }

        publishItemButton.setOnClickListener {
            findNavController().navigate(R.id.action_homepageFragment_to_publishItemFragment)
        }

        cartButton.setOnClickListener {
            findNavController().navigate(R.id.action_homepageFragment_to_cartFragment)
        }

        inboxButton.setOnClickListener {
            findNavController().navigate(R.id.action_HomepageFragment_to_inboxFragment)
        }

        // Set search and filter listeners
        setSearchAndFilterListeners()

        return view
    }

    private fun loadUserData() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val name = documentSnapshot.getString("username")
                        greetingBox.text = "Welcome, $name!"
                    } else {
                        greetingBox.text = "Welcome!"
                        Log.w("HomepageFragment", "User document not found.")
                    }
                }
                .addOnFailureListener { e ->
                    greetingBox.text = "Welcome!"
                    Log.e("HomepageFragment", "Failed to load user data: ${e.message}", e)
                }
        } else {
            greetingBox.text = "Welcome!"
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
                        // Log.d("FetchItems", "Fetched item: ${item.title}, imageUri: ${item.imageUri}")
                        item.id = document.id
                        items.add(item)
                    }
                }
                adapter.updateItems(items)
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
                priceHighSwitch.isChecked = false
            }
            filterAndSearchItems(searchBox.query.toString())
        }
        priceHighSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                priceLowSwitch.isChecked = false
            }
            filterAndSearchItems(searchBox.query.toString())
        }
    }

    private fun filterAndSearchItems(query: String = "") {
        val filteredItems = items.filter { item ->
            val matchesQuery = item.title.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
            val matchesNew = !newSwitch.isChecked || item.label.equals("new", ignoreCase = true)
            val matchesNearby = !nearbySwitch.isChecked || isNearby(item)
            matchesQuery && matchesNew && matchesNearby
        }

        val sortedItems = when {
            priceLowSwitch.isChecked -> filteredItems.sortedBy { it.price }
            priceHighSwitch.isChecked -> filteredItems.sortedByDescending { it.price }
            else -> filteredItems
        }

        adapter.updateItems(sortedItems)
    }

    private fun isNearby(item: PublishedItem): Boolean {
        return true // Replace with actual location checking
    }


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
}

