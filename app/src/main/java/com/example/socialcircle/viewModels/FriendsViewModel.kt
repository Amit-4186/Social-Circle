package com.example.socialcircle.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.socialcircle.models.ProfileDetails
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow

class FriendsViewModel: ViewModel() {

    private val db = Firebase.firestore
    val user = FirebaseAuth.getInstance().currentUser!!
    private val _friendList = MutableStateFlow<List<ProfileDetails>>(emptyList())

    val friendList = _friendList

    init{
        fetchFriendList { friendIds->
            fetchFriendProfiles(friendIds)
        }
    }

    fun fetchFriendList(onResult: (List<String>)->Unit){
        db.collection("UserProfiles").document(user.uid).collection("Friends")
            .get()
            .addOnSuccessListener { snapshot ->
                val friendIds = snapshot.documents.mapNotNull {doc-> doc.getString("userId")}  // Gives you the list of ids
                onResult(friendIds)
            }
    }

    private fun fetchFriendProfiles(friendIds: List<String>) {
        if (friendIds.isEmpty()) {
            _friendList.value = emptyList()
            return
        }

        val chunks = friendIds.chunked(10)
        val totalChunks = chunks.size
        val tempList = mutableListOf<ProfileDetails>()
        var completedChunks = 0

        Log.d("mine", chunks.toString())
        for (chunk in chunks) {
            db.collection("UserProfiles")
                .whereIn(FieldPath.documentId(), chunk) //
                .get()
                .addOnSuccessListener { result ->
                    val users = result.documents.mapNotNull { doc ->
                        doc.toObject(ProfileDetails::class.java)
                    }
                    Log.d("mine", result.size().toString())
                    tempList.addAll(users)
                    completedChunks++

                    if (completedChunks == totalChunks) {
                        _friendList.value = tempList
                    }
                }
                .addOnFailureListener {
                    completedChunks++
                    if (completedChunks == totalChunks) {
                        _friendList.value = tempList
                    }
                }
        }
    }
}
