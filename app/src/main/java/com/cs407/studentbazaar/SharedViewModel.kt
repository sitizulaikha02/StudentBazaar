package com.cs407.studentbazaar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cs407.studentbazaar.adapters.PublishedItem

class SharedViewModel : ViewModel() {

    // User profile details
    private val _name = MutableLiveData<String>()
    private val _username = MutableLiveData<String>()
    private val _bio = MutableLiveData<String>()

    val name: LiveData<String> get() = _name
    val username: LiveData<String> get() = _username
    val bio: LiveData<String> get() = _bio

    // Methods to update profile data
    fun setName(newName: String) {
        _name.value = newName
    }

    fun setUsername(newUsername: String) {
        _username.value = newUsername
    }

    fun setBio(newBio: String) {
        _bio.value = newBio
    }

    // LiveData for all items
    private val _items = MutableLiveData<List<PublishedItem>>(emptyList())
    val items: LiveData<List<PublishedItem>> get() = _items

    // LiveData for removed items
    private val _removedItems = MutableLiveData<List<PublishedItem>>(emptyList())
    val removedItems: LiveData<List<PublishedItem>> get() = _removedItems

    // Update the list of all items
    fun setItems(newItems: List<PublishedItem>) {
        _items.value = newItems
    }


    fun removeItems(purchasedItems: List<PublishedItem>) {
        val currentItems = _items.value?.toMutableList() ?: mutableListOf()

        // Remove items whose IDs match with the purchasedItems' IDs
        val purchasedItemIds = purchasedItems.map { it.id } // Extract IDs from purchased items
        currentItems.removeAll { item -> purchasedItemIds.contains(item.id) }

        _items.value = currentItems // Update the remaining items
        _removedItems.value = purchasedItems // Track removed items
    }


    // Add items dynamically if needed
    fun addItem(newItem: PublishedItem) {
        val currentItems = _items.value?.toMutableList() ?: mutableListOf()
        currentItems.add(newItem)
        _items.value = currentItems
    }
}
