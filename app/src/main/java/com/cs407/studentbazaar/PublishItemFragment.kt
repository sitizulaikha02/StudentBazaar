package com.cs407.studentbazaar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class PublishItemFragment : Fragment() {

    private lateinit var itemImage: ImageView
    private lateinit var itemTitle: EditText
    private lateinit var itemPrice: EditText
    private lateinit var itemLabel: EditText
    private lateinit var itemCondition: EditText
    private lateinit var itemDescription: EditText
    private lateinit var listItemButton: Button

    // Initialize Firestore and Firebase Storage
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var imageUri: Uri // To store the selected image URI

    private val PICK_IMAGE_REQUEST = 71

    private lateinit var backButton: ImageButton
    private lateinit var profileButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_publish_item, container, false)

        // Link UI components (updated to EditText for user input fields)
        itemImage = view.findViewById(R.id.itemImage)
        itemTitle = view.findViewById(R.id.itemTitle) // Changed to EditText for user input
        itemPrice = view.findViewById(R.id.itemPrice) // Changed to EditText for user input
        itemLabel = view.findViewById(R.id.itemLabel) // Changed to EditText for user input
        itemCondition = view.findViewById(R.id.itemCondition) // Changed to EditText for user input
        itemDescription = view.findViewById(R.id.itemDescription) // Changed to EditText for user input
        listItemButton = view.findViewById(R.id.listItemButton)

        // Initialize buttons
        backButton = view.findViewById(R.id.button_back)
        profileButton = view.findViewById(R.id.imageButton2)

        // Set button click listener to publish item
        listItemButton.setOnClickListener {
            publishItem()
        }

        // Back button click listener
        backButton.setOnClickListener {
            // Navigate back to the previous fragment (or home)
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Profile button click listener
        profileButton.setOnClickListener {
            // Navigate to UserProfileFragment using the action
            findNavController().navigate(R.id.action_publishItemFragment_to_userProfileFragment)
        }


        // Set click listener to select an image for the item
        itemImage.setOnClickListener {
            openGallery()
        }

        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Handle image selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == -1) {
            imageUri = data?.data ?: return
            itemImage.setImageURI(imageUri)
        }
    }

    private fun publishItem() {
        // Get text from fields
        val title = itemTitle.text.toString().trim()
        val priceStr = itemPrice.text.toString().trim()
        val label = itemLabel.text.toString().trim()
        val condition = itemCondition.text.toString().trim()
        val description = itemDescription.text.toString().trim()

        // Validate input
        if (title.isEmpty() || priceStr.isEmpty() || label.isEmpty() || condition.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert price to a number (Double)
        val price = priceStr.toDoubleOrNull()
        if (price == null) {
            Toast.makeText(requireContext(), "Please enter a valid price.", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current user's ID from FirebaseAuth
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Create item data with user ID
        val itemData: HashMap<String, Any> = hashMapOf(
            "title" to title,
            "price" to price,
            "label" to label,
            "condition" to condition,
            "description" to description,
            "userId" to userId // Use actual user ID from Firebase Authentication
        )

        // If an image was selected, upload it to Firebase Storage and get the URL
        if (this::imageUri.isInitialized) {
            val storageRef: StorageReference = storage.reference.child("item_images/${userId}_${System.currentTimeMillis()}")
            val uploadTask = storageRef.putFile(imageUri)

            uploadTask.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Add the image URL to the item data
                    itemData["imageUrl"] = uri.toString()

                    // Add item to Firestore
                    addItemToFirestore(itemData)
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        } else {
            // No image selected, just save the data without the image
            addItemToFirestore(itemData)
        }
    }

    private fun addItemToFirestore(itemData: HashMap<String, Any>) {
        db.collection("items")
            .add(itemData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Item published successfully!", Toast.LENGTH_SHORT).show()
                // Clear fields after successful publish
                clearFields()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to publish item: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        itemTitle.text.clear()
        itemPrice.text.clear()
        itemLabel.text.clear()
        itemCondition.text.clear()
        itemDescription.text.clear()
    }
}
