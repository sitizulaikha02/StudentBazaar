package com.cs407.studentbazaar.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    // Insert a new user into the database
    @Insert(entity = User::class)
    suspend fun insert(user: User)

    // Query to get a User by their userName
    @Query("SELECT * FROM user WHERE email = :email")
    suspend fun getByEmail(email: String): User

    // Update username, name, and bio based on email
    @Query("UPDATE user SET username = :newUsername, name = :name, bio = :bio WHERE email = :email")
    suspend fun updateProfile(email: String, newUsername: String, name: String, bio: String)
}