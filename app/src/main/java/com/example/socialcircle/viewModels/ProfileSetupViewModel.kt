package com.example.socialcircle.viewModels

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.example.socialcircle.models.ProfileDetails
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProfileSetupViewModel: ViewModel(){

    private val db = Firebase.firestore
    private val storageRef = FirebaseStorage.getInstance().reference

    private val _user = MutableStateFlow<ProfileDetails>(ProfileDetails())

    val user = _user.asStateFlow()

    fun updateUid(){
        _user.value = _user.value.copy(uid = FirebaseAuth.getInstance().currentUser!!.uid)//ProfileDetails(uid = )
    }

    fun updatePhoneNumber(phone: String): Boolean {
        if(phone.matches(Regex("^(\\+91)?[6-9]\\d{9}$"))) {
            _user.value = _user.value.copy(phoneNumber = phone)
            return true
        }
        else {
            return false
        }
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

    fun checkIfUsernameExists(
        username: String,
        onResult: (Boolean) -> Unit
    ) {
        db.collection("UserNames").document(username).get()
            .addOnSuccessListener { document ->
                onResult(document.exists())
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun upload(){
        val pathRef = storageRef.child("Profile_Pictures/${generateUniqueImageName()}")

        pathRef.putFile(user.value.photoUrl!!.toUri())
            .addOnSuccessListener {
                pathRef.downloadUrl
                    .addOnSuccessListener { downloadUrl ->
                        updateProfilePic(downloadUrl.toString())
                        db.collection("UserProfiles").document(_user.value.uid)
                            .set(_user.value)
                            .addOnSuccessListener { documentReference ->
                                Log.d("mine", "document added with id ${_user.value.userName}")
                                db.collection("UserNames")
                                    .document(_user.value.userName)
                                    .set(mapOf("createdAt" to Timestamp.now()))
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

    suspend fun uploadOnFireStore(): Boolean {
        val imageName = generateUniqueImageName()
        return try {
            // Step 1: Upload image
            val imageRef = storageRef.child("Profile_Pictures/${imageName}")
            imageRef.putFile(_user.value.photoUrl!!.toUri()).await()

            // Step 2: Get download URL
            val downloadUrl = imageRef.downloadUrl.await().toString()

            // Step 3: Update user object with new profile pic
            updateProfilePic(downloadUrl)

            // Step 4: Perform Firestore writes in a batch
            db.runBatch { batch ->
                val userProfileRef = db.collection("UserProfiles").document(_user.value.uid)
                batch.set(userProfileRef, _user.value)

                val userNameRef = db.collection("UserNames").document(_user.value.userName)
                batch.set(userNameRef, mapOf("createdAt" to Timestamp.now()))
            }.await()

            Log.d("mine", "✅ User profile and username uploaded successfully")
            true
        } catch (e: Exception) {
            Log.e("mine", "❌ Error uploading profile: ${e.message}", e)

            // Optional cleanup: delete uploaded image if DB failed
            try {
                val imageRef = storageRef.child("Profile_Pictures/${imageName}")
                imageRef.delete().await()
            } catch (ex: Exception) {
                Log.e("mine", "⚠️ Error deleting profile pic: ${ex.message}", ex)
            }

            false
        }
    }


    private fun generateUniqueImageName(extension: String = "jpg"): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString()
        return "IMG_${timestamp}_$uuid.$extension"
    }
}