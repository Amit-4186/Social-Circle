package com.example.socialcircle.models

import com.google.firebase.Timestamp

data class ChatMessage(
    val senderId: String = "",
    val receiverId: String= "",
    val text: String = "",
    val timestamp: Timestamp? = null
)
