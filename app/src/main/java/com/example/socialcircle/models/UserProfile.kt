package com.example.socialcircle.models

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val birthDate: Timestamp? = null,
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val userName: String = "",
    val friendCount: Int = 0
)