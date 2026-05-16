package com.train.app.data.models

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "User",
    val imageUrl: String = "",
    val category: String = "Strength",
    val likes: Int = 0,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)