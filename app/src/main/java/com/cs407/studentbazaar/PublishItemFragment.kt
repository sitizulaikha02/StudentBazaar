package com.cs407.studentbazaar

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import kotlin.random.Random

class PublishItemFragment : Fragment() {

    private lateinit var itemImage: ImageView
    private lateinit var itemTitle: EditText
    private lateinit var itemPrice: EditText
    private lateinit var itemLabel: EditText
    private lateinit var itemCondition: EditText
    private lateinit var itemDescription: EditText
    private lateinit var listItemButton: Button
    private var imageUriString: String? = null

    private val PICK_IMAGE_REQUEST = 71

    private lateinit var backButton: ImageButton
    private lateinit var profileButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_publish_item, container, false)

        itemImage = view.findViewById(R.id.itemImage)
        itemTitle = view.findViewById(R.id.itemTitle)
        itemPrice = view.findViewById(R.id.itemPrice)
        itemLabel = view.findViewById(R.id.itemLabel)
        itemCondition = view.findViewById(R.id.itemCondition)
        itemDescription = view.findViewById(R.id.itemDescription)
        listItemButton = view.findViewById(R.id.listItemButton)

        backButton = view.findViewById(R.id.button_back)
        profileButton = view.findViewById(R.id.imageButton2)

        listItemButton.setOnClickListener { publishItem() }

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        profileButton.setOnClickListener {
            findNavController().navigate(R.id.action_publishItemFragment_to_userProfileFragment)
        }

        itemImage.setOnClickListener {
            openGallery()
        }

        return view
    }

    // Open Gallery to pick an image
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Handle the selected image and display it
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == -1) {
            val imageUri = data?.data ?: return
            Log.d("imageUri: ", "$imageUri")
            itemImage.setImageURI(imageUri)  // Display selected image in ImageView
            imageUriString = imageUri.toString()
        }
    }

    private fun publishItem() {
        val title = itemTitle.text.toString().trim()
        val priceStr = itemPrice.text.toString().trim()
        val label = itemLabel.text.toString().trim()
        val condition = itemCondition.text.toString().trim()
        val description = itemDescription.text.toString().trim()
        val sharedPref = requireContext().getSharedPreferences("WhoAmI", Context.MODE_PRIVATE)
        val userId =
            sharedPref.getString("USER_ID", null)?.trim() ?: "" // Fetch userId with a default value of null

        // Validate fields
        if (title.isEmpty() || priceStr.isEmpty() || label.isEmpty() || condition.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null) {
            Toast.makeText(requireContext(), "Please enter a valid price.", Toast.LENGTH_SHORT).show()
            return
        }

        val itemData: HashMap<String, Any> = hashMapOf(
            "title" to title,
            "price" to price,
            "label" to label,
            "condition" to condition,
            "description" to description,
            "userId" to userId,
            "imageUri" to (imageUriString ?: ""),
            "timestamp" to System.currentTimeMillis()
        )

        addItemToFirestore(itemData)
    }

    private fun addItemToFirestore(itemData: HashMap<String, Any>) {
        val db = FirebaseFirestore.getInstance()
        db.collection("items")
            .add(itemData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Item published successfully!", Toast.LENGTH_SHORT).show()
                clearFields() // Clear the input fields after successful submission
                findNavController().navigate(R.id.action_publishItemFragment_to_homepageFragment)  // Adjust the ID as needed
            }
            .addOnFailureListener { e ->
                Log.e("PublishItem", "Failed to publish item: ${e.message}")
                Toast.makeText(requireContext(), "Failed to publish item: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Clear the input fields
    private fun clearFields() {
        itemTitle.text.clear()
        itemPrice.text.clear()
        itemLabel.text.clear()
        itemCondition.text.clear()
        itemDescription.text.clear()
    }
}
