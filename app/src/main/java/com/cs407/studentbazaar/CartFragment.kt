package com.cs407.studentbazaar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.studentbazaar.R
import com.cs407.studentbazaar.CartViewModel
import com.cs407.studentbazaar.adapters.PublishedItem
import androidx.navigation.fragment.findNavController

class CartFragment : Fragment() {

    private lateinit var cartViewModel: CartViewModel
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var checkoutButton: Button
    private lateinit var backButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        // Initialize ViewModel
        cartViewModel = ViewModelProvider(requireActivity())[CartViewModel::class.java]

        // Bind views
        cartRecyclerView = view.findViewById(R.id.recyclerView3)
        checkoutButton = view.findViewById(R.id.checkoutButton)
        backButton = view.findViewById(R.id.button_back)

        // Set up RecyclerView
        cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = CartAdapter(
            onRemoveClicked = { item ->
                cartViewModel.removeItem(item) // Remove item from the ViewModel
            }
        )
        cartRecyclerView.adapter = adapter

        // Observe cart items from ViewModel
        cartViewModel.cartItems.observe(viewLifecycleOwner, Observer { items ->
            if (items.isEmpty()) {
                cartRecyclerView.visibility = View.GONE
                checkoutButton.visibility = View.GONE
            } else {
                cartRecyclerView.visibility = View.VISIBLE
                checkoutButton.visibility = View.VISIBLE
                adapter.updateItems(items)
            }
        })

        // Observe the total price from the ViewModel (optional)
        cartViewModel.totalPrice.observe(viewLifecycleOwner, Observer { total ->
            // Update UI or show the total price somewhere if needed
        })

        // Checkout button listener
        checkoutButton.setOnClickListener {
            // Handle checkout logic, e.g., navigate to a checkout screen
        }

        // Back Button: Navigate to the homepage
        backButton.setOnClickListener {
            // Navigate to the homepage (e.g., home fragment or main activity)
            findNavController().navigate(R.id.action_cartFragment_to_homeFragment) // Replace with your homepage fragment
        }


        return view
    }
}
