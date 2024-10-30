package com.cs407.studentbazaar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class HomepageFragment : Fragment() {

    private lateinit var usernameButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_homepage, container, false)

        // Initialize the button
        usernameButton = view.findViewById(R.id.usernameButton)

        // Set OnClickListener on the button
        usernameButton.setOnClickListener {
            // Use Navigation Component to navigate to UserProfileFragment
            findNavController().navigate(R.id.action_homepageFragment_to_userProfileFragment)
        }

        return view
    }
}