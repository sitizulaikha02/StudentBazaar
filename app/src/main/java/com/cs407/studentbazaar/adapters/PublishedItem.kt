package com.cs407.studentbazaar.adapters

import java.io.Serializable

data class PublishedItem(
    var title: String = "",
    var price: Double = 0.0,
    var label: String = "",
    var condition: String = "",
    var description: String = "",
    var userId: String = "",
    var imageUri: String? = null,
    var timestamp: Long = 0,
    var id: String = ""
) : Serializable