package com.example.socialcircle.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.socialcircle.models.ProfileDetails
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ProfileViewModel: ViewModel(){

    private val db = Firebase.firestore
    private val storageRef = FirebaseStorage.getInstance().reference

    private val _user = MutableStateFlow(ProfileDetails())

    lateinit var profilePic: Uri
    val user = _user.asStateFlow()

    fun updatePhoneNumber(phone: String) {
        _user.value = _user.value.copy(phoneNumber = phone)
    }

    fun updateName(name: String) {
        _user.value = _user.value.copy(name = name)
    }

    fun updateUsername(username: String) {
        _user.value = _user.value.copy(userName = username)
    }

    fun updateBirthDate(date: Timestamp) {
        _user.value = _user.value.copy(birthDate = date)
    }

    fun updateProfilePic(url: String) {
        _user.value = _user.value.copy(photoUrl = url)
    }

    fun deleteProfilePic(pathRef: StorageReference){
        pathRef.delete()
    }

    fun uploadOnFireStore(){
        val pathRef = storageRef.child("Profile_Pictures/${generateUniqueImageName()}")

        pathRef.putFile(profilePic)
            .addOnSuccessListener {
                pathRef.downloadUrl
                    .addOnSuccessListener { downloadUrl ->
                        updateProfilePic(downloadUrl.toString())
                        db.collection("UserProfiles")
                            .add(_user.value)
                            .addOnSuccessListener { documentReference ->
                                Log.d("mine", "document added with id $documentReference")
                            }
                            .addOnFailureListener { e ->
                                Log.d("mine", "error in uploading", e)
                            }
                    }
                    .addOnFailureListener {
                        deleteProfilePic(storageRef)
                    }
            }
            .addOnFailureListener { e->
                Log.d("mine", "image not uploaded ${e.message}")
            }

    }

    private fun generateUniqueImageName(extension: String = "jpg"): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString()
        return "IMG_${timestamp}_$uuid.$extension"
    }

}