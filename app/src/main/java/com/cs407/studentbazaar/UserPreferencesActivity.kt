package com.cs407.studentbazaar

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class UserPreferencesActivity : AppCompatActivity() {

    private lateinit var cbTechnology: CheckBox
    private lateinit var cbFurniture: CheckBox
    private lateinit var cbClothing: CheckBox
    private lateinit var cbBooks: CheckBox
    private lateinit var cbAccessories: CheckBox
    private lateinit var cbSports: CheckBox
    private lateinit var btnShowGoods: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_preferences)

        cbTechnology = findViewById(R.id.cbTechnology)
        cbFurniture = findViewById(R.id.cbFurniture)
        cbClothing = findViewById(R.id.cbClothing)
        cbBooks = findViewById(R.id.cbBooks)
        cbAccessories = findViewById(R.id.cbAccessories)
        cbSports = findViewById(R.id.cbSports)
        btnShowGoods = findViewById(R.id.btnShowGoods)

        // Load saved preferences
        loadPreferences()

        // Set click listener to save preferences when button is clicked
        btnShowGoods.setOnClickListener {
            savePreferences()
            // Perform any additional actions, such as navigating to another activity
        }

        // Enable edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun savePreferences() {
        val sharedPref = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
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
        val sharedPref = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        cbTechnology.isChecked = sharedPref.getBoolean("Technology", false)
        cbFurniture.isChecked = sharedPref.getBoolean("Furniture", false)
        cbClothing.isChecked = sharedPref.getBoolean("Clothing", false)
        cbBooks.isChecked = sharedPref.getBoolean("Books", false)
        cbAccessories.isChecked = sharedPref.getBoolean("Accessories", false)
        cbSports.isChecked = sharedPref.getBoolean("Sports", false)
    }
}
