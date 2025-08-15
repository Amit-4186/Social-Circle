package com.example.socialcircle.models

import com.google.firebase.Timestamp


data class Chats(
    val chatId: String = "",
    val startsAt: Timestamp? = null,
    val lastRead: Map<String, Timestamp> = emptyMap()
)
