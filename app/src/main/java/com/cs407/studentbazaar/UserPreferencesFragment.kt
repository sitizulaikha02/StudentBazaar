package com.cs407.studentbazaar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class UserPreferencesFragment : Fragment() {

    private lateinit var cbTechnology: CheckBox
    private lateinit var cbFurniture: CheckBox
    private lateinit var cbClothing: CheckBox
    private lateinit var cbBooks: CheckBox
    private lateinit var cbAccessories: CheckBox
    private lateinit var cbSports: CheckBox
    private lateinit var btnShowGoods: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_preferences, container, false)

        cbTechnology = view.findViewById(R.id.cbTechnology)
        cbFurniture = view.findViewById(R.id.cbFurniture)
        cbClothing = view.findViewById(R.id.cbClothing)
        cbBooks = view.findViewById(R.id.cbBooks)
        cbAccessories = view.findViewById(R.id.cbAccessories)
        cbSports = view.findViewById(R.id.cbSports)
        btnShowGoods = view.findViewById(R.id.btnShowGoods)

        // Load saved preferences
        loadPreferences()

        // Set click listener to save preferences when button is clicked
        btnShowGoods.setOnClickListener {
            savePreferences()
            // Navigate to UserProfileFragment after saving preferences
            findNavController().navigate(R.id.action_userPreferencesFragment_to_homepageFragment)
        }

        // Enable edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        return view
    }

    private fun savePreferences() {
        val sharedPref = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("Technology", cbTechnology.isChecked)
            putBoolean("Furniture", cbFurniture.isChecked)
            putBoolean("Clothing", cbClothing.isChecked)
            putBoolean("Books", cbBooks.isChecked)
            putBoolean("Accessories", cbAccessories.isChecked)
            putBoolean("Sports", cbSports.isChecked)
            apply()
        }
    }

    private fun loadPreferences() {
        val sharedPref = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        cbTechnology.isChecked = sharedPref.getBoolean("Technology", false)
        cbFurniture.isChecked = sharedPref.getBoolean("Furniture", false)
        cbClothing.isChecked = sharedPref.getBoolean("Clothing", false)
        cbBooks.isChecked = sharedPref.getBoolean("Books", false)
        cbAccessories.isChecked = sharedPref.getBoolean("Accessories", false)
        cbSports.isChecked = sharedPref.getBoolean("Sports", false)
    }
}