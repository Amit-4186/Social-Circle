package com.example.socialcircle.viewModels

import android.net.Uri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import com.example.socialcircle.models.DateModel
import com.example.socialcircle.models.UserProfile
import com.example.socialcircle.models.UserStatus
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import java.util.UUID

class ProfileViewModel : ViewModel(), DefaultLifecycleObserver{

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val user = auth.currentUser
    private val dbRef = db.collection("UserProfiles").document(user!!.uid)

    private val _profileState = MutableStateFlow<UserProfile?>(null)
    val profileState: StateFlow<UserProfile?> = _profileState

    init{
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        setStatus(UserStatus.OFFLINE)
    }

    override fun onStart(owner: LifecycleOwner) {
        setStatus(UserStatus.ONLINE)
    }

    override fun onCleared() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        super.onCleared()
    }

    fun setStatus(status: UserStatus){
        dbRef.update("status", status.name)
    }

    fun loadProfile() {
        if(user == null) {
            return
        }

        dbRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val userProfile = UserProfile(
                    uid = doc.getString("uid") ?: user.uid,
                    name = doc.getString("name") ?: "",
                    birthDate = doc.getTimestamp("birthDate"),
                    phoneNumber = doc.getString("phoneNumber") ?: "",
                    photoUrl = doc.getString("photoUrl") ?: "",
                    userName = doc.getString("userName") ?: ""
                )

                dbRef.collection("Friends").get().addOnSuccessListener { friendsSnapshot ->
                    val count = friendsSnapshot.size()
                    _profileState.value = userProfile.copy(friendCount = count)
                }
            }
        }
    }

    fun updateName(newName: String) {
        updateField("name", newName)
    }

    fun updateBirthDate(newBirthDate: DateModel) {
        updateField("birthDate", createTimestamp(newBirthDate))
    }

    fun updatePhoneNumber(newPhone: String) {
        updateField("phoneNumber", newPhone)
    }

    fun updateUserName(newUsername: String) {
        updateField("userName", newUsername)
    }

    fun updateProfilePic(
        newImageUri: Uri,
        onResult: (Boolean) -> Unit
    ) {
        val storage = FirebaseStorage.getInstance().reference

        dbRef.get().addOnSuccessListener { snap ->
            val oldUrl = snap.getString("photoUrl")
            if (!oldUrl.isNullOrBlank()) {
                val oldRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldUrl)
                oldRef.delete()
            }

            val newPath = storage.child("Profile_Pictures/${generateUniqueImageName()}")
            newPath.putFile(newImageUri)
                .addOnSuccessListener {
                    newPath.downloadUrl
                        .addOnSuccessListener { dlUrl ->
                            dbRef.update("photoUrl", dlUrl.toString())
                                .addOnSuccessListener {
                                    loadProfile()
                                    onResult(true)
                                }
                                .addOnFailureListener { onResult(false) }
                        }
                        .addOnFailureListener { onResult(false) }
                }
                .addOnFailureListener { onResult(false) }
        }.addOnFailureListener {
            onResult(false)
        }
    }

    private fun updateField(field: String, value: Any) {
        dbRef.update(field, value).addOnSuccessListener {
            loadProfile()
        }
    }

    fun logout() {
        auth.signOut()
    }

    private fun generateUniqueImageName(extension: String = "jpg"): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString()
        return "IMG_${timestamp}_$uuid.$extension"
    }

    fun createTimestamp(dateModel: DateModel): Timestamp {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, dateModel.year)
            set(Calendar.MONTH, dateModel.month - 1)
            set(Calendar.DAY_OF_MONTH, dateModel.day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val date = calendar.time
        return Timestamp(date)
    }
}
