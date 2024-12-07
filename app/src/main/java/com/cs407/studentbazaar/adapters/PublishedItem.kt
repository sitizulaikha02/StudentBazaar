package com.cs407.studentbazaar.adapters

import java.io.Serializable

data class PublishedItem(
    val title: String = "",
    val price: Double = 0.0,
    val label: String = "",
    val condition: String = "",
    val description: String = "",
    val userId: String = "",
    val imageUri: String? = null,
    val timestamp: Long = 0,
    val id: String = ""
) : Serializable