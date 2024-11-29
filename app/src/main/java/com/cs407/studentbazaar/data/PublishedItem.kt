package com.cs407.studentbazaar.data

data class PublishedItem(
    val title: String = "",
    val price: Double = 0.0,
    val label: String = "",
    val condition: String = "",
    val description: String = "",
    val userId: String = "",
    val imageUri: String? = null, // This can be nullable if there's no image
    val timestamp: Long = 0
)