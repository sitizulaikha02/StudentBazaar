package com.cs407.studentbazaar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {

    // Nothing to change here!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Explicitly get the NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Handle the notification intent when the app is launched
        handleNotificationIntent(intent, navController)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Explicitly get the NavController again for the new intent
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Handle the notification intent when the app is already running
        handleNotificationIntent(intent, navController)
    }

    private fun handleNotificationIntent(intent: Intent?, navController: NavController) {
        intent?.let {
            val name = it.getStringExtra("name")
            val uid = it.getStringExtra("senderUid")
            val notificationType = it.getStringExtra("notificationType") // Example: data from the notification

            val bundle = Bundle().apply {
                putString("name", name)
                putString("uid", uid)
            }

            // Handle navigation based on the notification type
            when (notificationType) {
                "message" -> {
                    Log.d("MainActivity", "Navigating to chat fragment.")

                    // Pop the back stack to ensure a fresh navigation
                    navController.popBackStack(R.id.chatFragment, true)

                    // Navigate to the chat fragment
                    navController.navigate(R.id.chatFragment, bundle)
                }

                "following" -> {
                    Log.d("MainActivity", "Navigating to user profile fragment.")

                    // Pop the back stack to ensure a fresh navigation
                    navController.popBackStack(R.id.userProfileFragment, true)

                    // Navigate to the user profile fragment
                    navController.navigate(R.id.userProfileFragment, bundle)
                }

                else -> Log.e("MainActivity", "Unknown notification type: $notificationType")
            }
        }
    }
}