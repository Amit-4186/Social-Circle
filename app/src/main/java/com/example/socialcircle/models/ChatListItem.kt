package com.example.socialcircle.models

import com.google.firebase.Timestamp

data class ChatListItem(
    val chatId: String = "",
    val otherUserId: String = "",
    val profileImageUrl: String = "",
    val name: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp? = null,
    val unreadCount: Int = 0,
    val isTemporary: Boolean = true,
    val expireAt: Timestamp? = null
)
