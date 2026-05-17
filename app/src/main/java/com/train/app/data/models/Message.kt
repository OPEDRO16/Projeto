package com.train.app.data.models

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val sharedRoutineId: String? = null,
    val sharedRoutineName: String? = null,
    val sharedRoutineExercises: List<String>? = null,
    val sharedPostId: String? = null,
    val sharedPostContent: String? = null,
    val sharedPostAuthorName: String? = null,
    val sharedPostImageUrl: String? = null,
    val sharedWorkoutSessionId: String? = null,
    val sharedPostUserId: String? = null
)