package com.cs407.studentbazaar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput

class NotificationHelper private constructor() {

    // Declare a unique ID for each notification instance
    private var notificationId: Int = 0

    // Declare variables for sender name and message content
    private var sender: String? = null
    private var message: String? = null

    companion object {
        @Volatile
        private var instance: NotificationHelper? = null

        fun getInstance(): NotificationHelper {
            return instance ?: synchronized(this) {
                instance ?: NotificationHelper().also { instance = it}
            }
        }

        // Declare CHANNEL_ID: a unique identifier for the notification channel.
        val CHANNEL_ID = "channel_chat"
    }

    /**
     * The createNotificationChannel function creates a notification channel.
     * Notification channels are required for apps targeting android O (API level 26) and above
     * to send notifications. It allows grouping of notifications by category.
     */
    public fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Fetch the channel name and description from resources.
            // - Use context.getString(R.string.channel_name) to obtain the name of the channel.
            // - Use context.getString(R.string.channel_description) for the description.
            val channelName = context.getString(R.string.channel_name)
            val channelDesc = context.getString(R.string.channel_description)

            // Define the notification importance level.
            // - Use NotificationManager.IMPORTANCE_DEFAULT
            val importance = NotificationManager.IMPORTANCE_HIGH

            // Define and create a NotificationChannel instance with an ID, name
            //  and importance level.
            // - Use CHANNEL_ID as the unigue identifier for this channel.
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                setDescription(channelDesc)
            }

            // Register the channel with the system.
            // - Use context.getSystemService(NotificationManager::class.java) to get the
            // NotificationManager instance.
            // - Check if notificationManager is not null before calling createNotificationChannel(channel)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun setNotificationContent(sender: String, message: String) {
        // Set the sender field with the provided sender name
        this.sender = sender

        // Set the message field with the provided message content
        this.message = message

        // Increment notificationId to ensure each notification is unique
        notificationId++
    }

    /**
     * Displays a notification in the notification drawer.
     *
     * This function checks if the app has permission to post notifications, then builds and displays
     * a notification using the specified content title and text. The notification includes a small
     * icon and is set to be visible only on a secure lock screen to protect privacy.
     *
     * @param context The context in which the function is called, typically an Activity or
     *                  Application context.
     */
    fun showNotification(context: Context, senderUid: String, notificationType: String) {
        // Ensure that the app has the required POST_NOTIFICATIONS permission before proceeding.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, exit the function without showing the notification
            return
        }

        // Create an Intent to open MainActivity with the messageId
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("name", sender) // Example: Include sender's name
            putExtra("senderUid", senderUid) // Include sender's UID
            putExtra("notificationType", notificationType) // Include notification type
        }

        // Create a PendingIntent for the notification click
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId, // Use the unique notificationId
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Set up a NotificationCompat.Builder instance to create and customize the notification.
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            // Set the small icon for the notification, which will appear in the status bar
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            // Set the title of the notification using the sender's name.
            .setContentTitle(sender)
            // Set the content text of the notification using the message content.
            .setContentText(message)
            // Set the notification priority; PRIORITY_DEFAULT keeps the notification non-intrusive.
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Auto-dismiss on click
            .setContentIntent(pendingIntent) // Attach the PendingIntent


        // Get a NotificationManagerCompat instance to issue the notification
        val notificationManager = NotificationManagerCompat.from(context)
        // Send out the notification with the unique notificationId
        notificationManager.notify(notificationId, builder.build())
    }
}