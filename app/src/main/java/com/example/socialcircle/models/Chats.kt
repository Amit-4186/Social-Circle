package com.example.socialcircle.models

import com.google.firebase.Timestamp


data class Chats(
    val chatId: String = "",
    val startsAt: Timestamp? = null
//    val isTemporary: Boolean = false,
//    val expireAt: Timestamp? = null         // Nullable: only for temporary chats
)
