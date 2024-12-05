package com.cs407.studentbazaar.notifications

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import java.io.InputStream
import com.cs407.studentbazaar.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OAuthTokenProvider(private val context: Context) {

    suspend fun getAccessToken(): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Load the service account key JSON file from res/raw
                val inputStream: InputStream = context.resources.openRawResource(R.raw.service_account_key)

                // Use GoogleCredentials to generate the access token
                val credentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

                credentials.refreshIfExpired() // Refresh the token if it's expired
                credentials.accessToken.tokenValue // Return the token
            } catch (e: Exception) {
                Log.d("OAuthTokenProvider", "Exception occurred: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }
}
