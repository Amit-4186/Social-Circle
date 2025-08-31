package com.example.socialcircle.models

import com.google.firebase.Timestamp

data class ProfileDetails(
    val uid: String = "",
    val name: String = "",
    val userName: String = "",
    val phoneNumber: String = "",
    val birthDate: Timestamp? = Timestamp.now(),
    val photoUrl: String? = null
)