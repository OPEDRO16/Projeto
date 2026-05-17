package com.train.app.data.models

data class Post(
    var id: String = "",
    var userId: String = "",
    var userName: String = "User",
    var imageUrl: String = "",
    var category: String = "Treino",
    var likes: Int = 0,
    var description: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    
    // Workout specifics
    var workoutSessionId: String? = null,
    var workoutName: String? = null,
    var workoutVolume: Float? = null,
    var workoutDuration: Int? = null,
    
    // Social extras
    var likedBy: List<String> = emptyList(),
    var visibility: String = "public", // "public" ou "friends"
    var commentsCount: Int = 0
)