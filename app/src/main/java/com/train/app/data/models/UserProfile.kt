package com.train.app.data.models

data class UserProfile(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var bio: String? = null,
    var photoUrl: String? = null,
    var friends: List<String> = emptyList(),
    var friendRequests: List<String> = emptyList(), // UIDs das pessoas que enviaram pedido para mim
    var isPremium: Boolean = false,
    var subscriptionTier: String = "FREE", // "FREE", "PRO", "MASTER"
    var appTheme: String = "DARK", // "DARK", "LIGHT", "CUSTOM"
    var customAccentColor: String = "#0A62D0" // For MASTER custom dynamic colors
)
