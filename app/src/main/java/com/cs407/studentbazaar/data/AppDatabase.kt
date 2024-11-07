package com.cs407.studentbazaar.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.cs407.studentbazaar.R

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    // Provide DAOs to access the database
    abstract fun userDao(): UserDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Get or create the database instance
        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    context.getString(R.string.app_database) // Database name from resources
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}