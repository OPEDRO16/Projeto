package com.train.app.data.models

data class UserProfile(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var bio: String? = null,
    var photoUrl: String? = null,
    var friends: List<String> = emptyList(),
    var friendRequests: List<String> = emptyList() // UIDs das pessoas que enviaram pedido para mim
)
