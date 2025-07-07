package com.example.socialcircle.models

import com.google.firebase.Timestamp


data class Chats(
    val chatId: String = "",
    val userIds: List<String> = emptyList(),   // [senderId, receiverId]
    val isTemporary: Boolean = false,
    val expireAt: Timestamp? = null         // Nullable: only for temporary chats
)
