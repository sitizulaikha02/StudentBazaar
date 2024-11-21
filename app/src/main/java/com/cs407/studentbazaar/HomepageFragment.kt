package com.cs407.studentbazaar

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import kotlin.getValue

class HomepageFragment : Fragment() {

    private lateinit var usernameButton: ImageButton
    private lateinit var publishItemButton: Button

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
        usernameButton = view.findViewById(R.id.usernameButton)
        publishItemButton = view.findViewById(R.id.publish_item)
        // Set OnClickListener on the button
        usernameButton.setOnClickListener {
            // Use Navigation Component to navigate to UserProfileFragment
            findNavController().navigate(R.id.action_homepageFragment_to_userProfileFragment)
        }

        publishItemButton.setOnClickListener {
            findNavController().navigate(R.id.action_homepageFragment_to_publishItemFragment)
        }

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
}