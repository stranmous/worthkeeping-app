package com.worthkeeping.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class FeedbackRepository {
    private val client = OkHttpClient()

    private val supabaseUrl = com.worthkeeping.app.BuildConfig.SUPABASE_URL
    private val anonKey = com.worthkeeping.app.BuildConfig.SUPABASE_ANON_KEY

    suspend fun submitFeedback(message: String, androidVersion: String, appVersion: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("message", message)
                put("android_version", androidVersion)
                put("app_version", appVersion)
            }

            val requestBody = json.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(supabaseUrl)
                .post(requestBody)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            // Error silently ignored for release
            false
        }
    }
}
