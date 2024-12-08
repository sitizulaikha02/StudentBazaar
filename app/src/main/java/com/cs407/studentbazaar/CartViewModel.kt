package com.cs407.studentbazaar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cs407.studentbazaar.adapters.PublishedItem // <-- Add this line

class CartViewModel : ViewModel() {
    // LiveData for storing cart items
    private val _cartItems = MutableLiveData<List<PublishedItem>>()
    val cartItems: LiveData<List<PublishedItem>> get() = _cartItems

    // LiveData for storing total price
    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> get() = _totalPrice

    init {
        // Initialize cart with an empty list and total price as 0
        _cartItems.value = emptyList()
        _totalPrice.value = 0.0
    }

    // Add an item to the cart and recalculate the total price
    fun addItem(item: PublishedItem) {
        val currentItems = _cartItems.value?.toMutableList() ?: mutableListOf()
        currentItems.add(item)
        _cartItems.value = currentItems
        recalculateTotal()
    }

    // Remove an item from the cart and recalculate the total price
    fun removeItem(item: PublishedItem) {
        val currentItems = _cartItems.value?.toMutableList() ?: mutableListOf()
        currentItems.remove(item)
        _cartItems.value = currentItems
        recalculateTotal()
    }

    // Clear all items from the cart and reset the total price
    fun clearCart() {
        _cartItems.value = emptyList()
        _totalPrice.value = 0.0
    }

    // Recalculate the total price based on the current items in the cart
    private fun recalculateTotal() {
        val total = _cartItems.value?.sumOf { it.price } ?: 0.0
        _totalPrice.value = total
    }
}
