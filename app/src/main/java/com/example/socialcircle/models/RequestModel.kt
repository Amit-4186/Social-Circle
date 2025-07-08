package com.example.socialcircle.models

import com.google.firebase.Timestamp


data class RequestModel(
    val fromUserId: String = "",
    val toUserId: String = "",
    val timestamp: Timestamp = Timestamp.now()
)