package com.vibewardrobe.data

data class WardrobeLook(
    val id: Int? = null,
    val title: String = "",
    val imageUrl: String? = null,
    val thumbnailUrl: String? = null,
    val style: String = "",
    val intensity: Float = 0.5f
)

data class GeminiResponse(
    val imageUrl: String?,
    val thumbnailUrl: String? = null
)

data class ProfileSettings(
    val displayName: String,
    val apiKey: String,
    val enableHaptics: Boolean,
    val isDarkTheme: Boolean,
    val avatarUrl: String? = null
)
