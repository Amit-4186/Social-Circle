package com.example.socialcircle.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ChatMessage(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null
)
