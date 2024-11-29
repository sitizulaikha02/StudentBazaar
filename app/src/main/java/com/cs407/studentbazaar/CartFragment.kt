package com.cs407.studentbazaar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView

class CartFragment : Fragment() {

    private lateinit var cartButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        // Initialize buttons
        cartButton = view.findViewById(R.id.button_back)

        // Back button click listener
        cartButton.setOnClickListener {
            // Navigate back to the previous fragment (or home)
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }
}