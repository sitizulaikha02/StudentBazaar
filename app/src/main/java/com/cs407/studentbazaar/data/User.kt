package com.cs407.studentbazaar.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [Index(
        value = ["email"], unique = true
    )]
)
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val email: String,
    val username: String,
    val name: String,
    val bio: String
)