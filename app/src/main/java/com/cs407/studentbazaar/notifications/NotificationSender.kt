package com.cs407.studentbazaar.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class NotificationSender(private val context: Context) {

    companion object {
        private const val FCM_URL = "https://fcm.googleapis.com/v1/projects/secondhandmarketplaceapp/messages:send"
    }

    fun sendNotificationToUser(recipientToken: String, title: String, body: String, senderUid: String?, notificationType: String?) {
        val tokenProvider = OAuthTokenProvider(context)

        // Use a coroutine to fetch the OAuth token and send the notification
        CoroutineScope(Dispatchers.Main).launch {
            val accessToken = tokenProvider.getAccessToken()
            if (accessToken != null) {
                sendNotificationToFCM(accessToken, recipientToken, title, body, senderUid, notificationType)
            } else {
                Log.e("OAuthTokenProvider", "Failed to fetch access token")
            }
        }


    }

    private fun sendNotificationToFCM(accessToken: String, token: String, title: String, body: String, senderUid: String?, notificationType: String?) {

        val json = """
            {
              "message": {
                "token": "$token",
                "notification": {
                  "title": "$title",
                  "body": "$body"
                },
                "data": {
                  "name": "$title",
                  "body": "$body",
                  "senderUid": "$senderUid",
                  "notificationType": "$notificationType" 
                }
              }
            }
        """.trimIndent()

        val client = OkHttpClient()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(FCM_URL)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $accessToken") // Use dynamic token
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("NotificationSender", "Notification sent successfully")
                } else {
                    Log.e("NotificationSender", "Error: ${response.body?.string()}")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("NotificationSender", "Error: ${e.message}")
            }
        })
    }
}
