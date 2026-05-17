package com.train.app.data.models

data class ChatRoom(
    val id: String = "",
    val name: String = "",
    @field:JvmField val isGroup: Boolean = false,
    val members: List<String> = emptyList(),
    val createdById: String = "",
    val lastMessageContent: String? = null,
    val lastMessageSender: String? = null,
    val lastMessageTime: Long = 0L
)
