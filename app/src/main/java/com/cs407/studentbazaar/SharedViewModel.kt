package com.cs407.studentbazaar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _name = MutableLiveData<String>()
    private val _username = MutableLiveData<String>()
    private val _bio = MutableLiveData<String>()

    val name: LiveData<String> get() = _name
    val username: LiveData<String> get() = _username
    val bio: LiveData<String> get() = _bio

    // Methods to update the data
    fun setName(newName: String) {
        _name.value = newName
    }

    fun setUsername(newUsername: String) {
        _username.value = newUsername
    }

    fun setBio(newBio: String) {
        _bio.value = newBio
    }
}