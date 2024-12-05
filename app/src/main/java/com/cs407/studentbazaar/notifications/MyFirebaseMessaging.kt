package com.cs407.studentbazaar.notifications
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.cs407.studentbazaar.ChatFragment
import com.cs407.studentbazaar.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.text.replace

class MyFirebaseMessaging : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM Token: $token")
        // Send the new token to your server
        saveTokenToFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        // Extract notification content from the FCM message
        val sender = remoteMessage.data["name"] ?: "Unknown Sender"
        val message = remoteMessage.data["body"] ?: "No message content"
        //val messageId = remoteMessage.data["messageId"] ?: ""
        val senderUid = remoteMessage.data["senderUid"] ?: ""
        val notificationType = remoteMessage.data["notificationType"] ?: ""

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if(firebaseUser != null && senderUid != firebaseUser.uid) {
            Log.d("Notification", "Notification displayed successfully via MyFirebaseMessaging")
            // Use NotificationHelper to show the notification
            NotificationHelper.getInstance().setNotificationContent(sender, message)
            NotificationHelper.getInstance().showNotification(this, senderUid, notificationType)
        }
    }

    private fun saveTokenToFirestore(token: String) {
        // Get the current user's ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            // Get the Firestore instance
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("Firestore", "Token successfully updated for user: $userId")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error updating token: ${e.message}")
                }

        } else {
            Log.e("Firestore", "Error: User is not authenticated.")
        }
    }

//    private fun sendNotification(message: mRemoteMessage) {
//        val user = mRemoteMessage.data["user"]
//        val icon = mRemoteMessage.data["icon"]
//        val body = mRemoteMessage.data["body"]
//        val title = mRemoteMessage.data["title"]
//
//        val notification = mRemoteMessage.notification
//        val j = user!!.replace("[\\D]".toRegex(), "").toInt()
//        val intent = Intent(this, ChatFragment::class.java)
//
//        val bundle = Bundle()
//        bundle.putString("userId", user)
//        intent.putExtras(bundle)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//
//        val pendingIntent = PendingIntent.getActivity(this, j, intent,
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
//        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        val oreoNotification = OreoNotification(this)
//
//
//        val builder: Notification.Builder = oreoNotification.getOreoNotification(title, body, pendingIntent)
//
//        var i = 0
//        if (j>0) {
//            i=j
//        }
//
//        oreoNotification.getManager!!.notify(i, builder.build)
//
//    }
//
//    private fun sendOreoNotification(mRemoteMessage: RemoteMessage) {
//        val user = mRemoteMessage.data["user"]
//        val icon = mRemoteMessage.data["icon"]
//        val body = mRemoteMessage.data["body"]
//        val title = mRemoteMessage.data["title"]
//
//        val notification = mRemoteMessage.notification
//        val j = user!!.replace("[\\D]".toRegex(), "").toInt()
//        val intent = Intent(this, ChatFragment::class.java)
//
//        val bundle = Bundle()
//        bundle.putString("userId", user)
//        intent.putExtras(bundle)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//
//        val pendingIntent = PendingIntent.getActivity(this, j, intent,
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
//        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        val oreoNotification = OreoNotification(this)
//
//
//        val builder: Notification.Builder = oreoNotification.getOreoNotification(title, body, pendingIntent)
//
//        var i = 0
//        if (j>0) {
//            i=j
//        }
//
//        oreoNotification.getManager!!.notify(i, builder.build)
//
//    }
}