package com.cs407.studentbazaar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cs407.studentbazaar.adapters.PublishedItem
import com.google.firebase.firestore.FirebaseFirestore

class ViewItemFragment : Fragment() {

    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var priceTextView: TextView
    private lateinit var labelTextView: TextView
    private lateinit var conditionTextView: TextView
    private lateinit var itemImageView: ImageView
    private lateinit var addToCartButton: Button
    private lateinit var messageSellerButton: Button
    private lateinit var backButton: ImageView
    private lateinit var cartViewModel: CartViewModel
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_item, container, false)

        // Bind views
        titleTextView = view.findViewById(R.id.itemTitleView)
        descriptionTextView = view.findViewById(R.id.itemDescView)
        priceTextView = view.findViewById(R.id.itemPriceView)
        labelTextView = view.findViewById(R.id.itemLabelView)
        conditionTextView = view.findViewById(R.id.itemConditionView)
        itemImageView = view.findViewById(R.id.itemImageView)
        addToCartButton = view.findViewById(R.id.addCartButton)
        messageSellerButton = view.findViewById(R.id.messageSellerButton)
        backButton = view.findViewById(R.id.button_back)

        cartViewModel = ViewModelProvider(requireActivity())[CartViewModel::class.java]

        // Set data from arguments
        arguments?.let {
            titleTextView.text = it.getString("title") ?: "No Title"
            descriptionTextView.text = it.getString("description") ?: "No Description"
            priceTextView.text = "$${it.getDouble("price", 0.0)}"
            labelTextView.text = it.getString("label") ?: "No Label"
            conditionTextView.text = it.getString("condition") ?: "Unknown Condition"

            // Load image using Glide
            val imageUri = it.getString("imageUri")
            Glide.with(requireContext())
                .load(imageUri)
                .error(R.drawable.default_image) // Placeholder for missing images
                .into(itemImageView)
        }

        addToCartButton.setOnClickListener {
            // Create a PublishedItem object from the data displayed on the screen
            val item = PublishedItem(
                title = titleTextView.text.toString(),
                description = descriptionTextView.text.toString(),
                price = priceTextView.text.toString()
                    .removePrefix("$")
                    .toDoubleOrNull() ?: 0.0, // Convert to double, fallback to 0.0 if invalid
                label = labelTextView.text.toString(),
                condition = conditionTextView.text.toString(),
                imageUri = arguments?.getString("imageUri") ?: "",
                userId = arguments?.getString("userId") ?: "",
                id = arguments?.getString("id") ?: ""
            )

            // Add the item to the cart using ViewModel
            cartViewModel.addItem(item)

            // Display a message to the user
            Toast.makeText(requireContext(), "${item.title} added to cart!", Toast.LENGTH_SHORT).show()
        }


        messageSellerButton.setOnClickListener {
            val db = FirebaseFirestore.getInstance()

            val userId = arguments?.getString("userId") ?: ""

            val userRef = db.collection("users").document(userId)

            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        if (name != null) {
                            val bundle = Bundle().apply {
                                putString("name", name)
                                putString("uid", userId)
                            }
                            findNavController().navigate(R.id.action_viewItemFragment_to_chatFragment, bundle)
                        } else {
                            // Handle the case where 'name' doesn't exist in the document
                            Log.e("Firestore", "Name field is missing in the document.")
                        }
                    } else {
                        // Handle the case where the document doesn't exist
                        Log.e("Firestore", "Document not found for userId: $userId")
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle Firestore fetch error
                    Log.e("Firestore", "Error fetching user data: ", exception)
                }
        }

        // Back Button: Navigate to the previous fragment
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack() // Pop the current fragment from the stack
        }

        return view
    }
}
