package com.cs407.studentbazaar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.cs407.studentbazaar.adapters.PublishedItem
import com.google.gson.Gson

class CartFragment : Fragment() {

    private lateinit var cartViewModel: CartViewModel
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var checkoutButton: Button
    private lateinit var backButton: ImageView
    private lateinit var totalPriceTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        // Initialize ViewModel
        cartViewModel = ViewModelProvider(requireActivity())[CartViewModel::class.java]

        // Bind views
        cartRecyclerView = view.findViewById(R.id.recyclerView3)
        checkoutButton = view.findViewById(R.id.checkoutButton)
        backButton = view.findViewById(R.id.button_back)
        totalPriceTextView = view.findViewById(R.id.totalPriceTextView)

        // Set up RecyclerView
        cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = CartAdapter(
            onRemoveClicked = { item ->
                cartViewModel.removeItem(item) // Remove item from the ViewModel
            }
        )
        cartRecyclerView.adapter = adapter

        // Observe cart items
        cartViewModel.cartItems.observe(viewLifecycleOwner, Observer { items ->
            if (items.isEmpty()) {
                cartRecyclerView.visibility = View.GONE
                checkoutButton.visibility = View.GONE
                totalPriceTextView.visibility = View.GONE
            } else {
                cartRecyclerView.visibility = View.VISIBLE
                checkoutButton.visibility = View.VISIBLE
                totalPriceTextView.visibility = View.VISIBLE
                adapter.updateItems(items)
            }
        })

        // Observe total price
        cartViewModel.totalPrice.observe(viewLifecycleOwner, Observer { total ->
            totalPriceTextView.text = "Total: $$total"
        })

        // Checkout button click listener
        checkoutButton.setOnClickListener {
            val totalPrice = cartViewModel.totalPrice.value ?: "0.00"
            val cartItems = cartViewModel.cartItems.value ?: emptyList()

            Log.d("CartFragment", "Total Price: $totalPrice")
            Log.d("CartFragment", "Cart Items: $cartItems")

            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Cart is empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cartItemsJson = Gson().toJson(cartItems) // Serialize to JSON

            val bundle = Bundle().apply {
                putString("totalPrice", totalPrice.toString())
                putString("cartItemsJson", cartItemsJson) // Pass JSON string
            }

            try {
                findNavController().navigate(R.id.action_cartFragment_to_paymentFragment, bundle)
            } catch (e: Exception) {
                Log.e("CartFragment", "Error navigating to PaymentFragment", e)
            }
        }

        // Back button click listener
        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_cartFragment_to_homepageFragment)
        }

        return view
    }
}
