package com.example.socialcircle.models

import com.google.firebase.Timestamp

data class ProfileDetails(
    val name: String = "",
    val userName: String = "",
    val phoneNumber: String = "",
    val birthDate: Timestamp? = null,
    val photoUrl: String = ""
)
