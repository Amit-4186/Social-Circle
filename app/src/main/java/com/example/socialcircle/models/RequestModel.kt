package com.example.socialcircle.models

import java.sql.Timestamp

data class RequestModel(
    val fromUserId: String = "",
    val toUserId: String = "",
    val timestamp: Timestamp = Timestamp(0)
)
