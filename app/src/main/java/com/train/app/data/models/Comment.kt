package com.train.app.data.models

import java.util.UUID

data class Comment(
    var id: String = UUID.randomUUID().toString(),
    var postId: String = "",
    var userId: String = "",
    var userName: String = "",
    var text: String = "",
    var timestamp: Long = System.currentTimeMillis()
)
