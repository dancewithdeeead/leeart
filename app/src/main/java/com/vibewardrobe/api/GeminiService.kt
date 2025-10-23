package com.vibewardrobe.api

import com.vibewardrobe.data.GeminiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object GeminiService {
    private const val BASE_URL = "https://api.banana.dev/gemini"
    private val client = OkHttpClient()

    suspend fun generateLook(
        prompt: String,
        baseImageUrl: String?,
        apiKey: String
    ): GeminiResponse? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext null

        val payload = JSONObject().apply {
            put("prompt", prompt)
            put("image_url", baseImageUrl)
        }
        val request = Request.Builder()
            .url("$BASE_URL/v1/generate")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body?.string() ?: return@use null
                val json = JSONObject(body)
                val imageUrl = json.optString("image_url", null)
                GeminiResponse(
                    imageUrl = imageUrl,
                    thumbnailUrl = json.optString("thumbnail_url", imageUrl)
                )
            }
        }.getOrNull()
    }
}
