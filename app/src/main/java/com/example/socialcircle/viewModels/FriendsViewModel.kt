package com.example.socialcircle.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.socialcircle.models.ProfileDetails
import com.example.socialcircle.models.RequestModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import java.sql.Timestamp

class FriendsViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser!!
    private val _friendList = MutableStateFlow<List<ProfileDetails>>(emptyList())
    private val _requestList = MutableStateFlow<List<ProfileDetails>>(emptyList())
//    private val _nearbyProfiles = MutableStateFlow<List<ProfileDetails>>(emptyList())

    val friendList = _friendList
    val requestList = _requestList
//    val nearbyProfile = _nearbyProfiles

    fun getFriendProfiles() {
        fetchFriendIds { idList ->
            fetchProfilesByIds(idList, _friendList)
        }
    }

    fun getFriendRequests() {
        fetchFriendRequestIds { idList ->
            fetchProfilesByIds(idList, _requestList)
        }
    }

//    fun getNearbyProfile(userIds: List<String>) {
//        fetchProfilesByIds(userIds, _nearbyProfiles)
//    }


    fun sendFriendRequest(
        toUserId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        db.collection("FriendRequests")
            .add(
                RequestModel(
                    fromUserid = user.uid,
                    toUserId = toUserId,
                    Timestamp(System.currentTimeMillis())
                ),
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }


    private fun fetchFriendIds(onResult: (List<String>) -> Unit) {
        db.collection("UserProfiles").document(user.uid).collection("Friends")
            .get()
            .addOnSuccessListener { snapshot ->
                val friendIds =
                    snapshot.documents.mapNotNull { doc -> doc.getString("userId") }  // Gives you the list of ids
                onResult(friendIds)
            }
    }

    private fun fetchFriendRequestIds(onResult: (List<String>) -> Unit) {
        db.collection("FriendRequests")
            .whereEqualTo("toUserId", user.uid)
            .get()
            .addOnSuccessListener { document ->
                val result = document.mapNotNull { doc ->
                    doc.getString("fromUserId")
                }
                onResult(result)
            }
            .addOnFailureListener { e ->
                Log.d("mine", "failed to fetch requests ${e.message}")
            }
    }

    private fun fetchProfilesByIds(
        userIds: List<String>,
        targetState: MutableStateFlow<List<ProfileDetails>>
    ) {
        if (userIds.isEmpty()) {
            targetState.value = emptyList()
            return
        }

        val chunks = userIds.chunked(10)
        val totalChunks = chunks.size
        val tempList = mutableListOf<ProfileDetails>()
        var completedChunks = 0

        for (chunk in chunks) {
            db.collection("UserProfiles")
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .addOnSuccessListener { result ->
                    val users = result.documents.mapNotNull { doc ->
                        doc.toObject(ProfileDetails::class.java)
                    }
                    tempList.addAll(users)
                    Log.d("mine", "successful ${result.size()}")
                    completedChunks++

                    if (completedChunks == totalChunks) {
                        targetState.value = tempList
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("mine", "failed: ${e.message}")
                    completedChunks++
                    if (completedChunks == totalChunks) {
                        targetState.value = tempList
                    }
                }
        }
    }


    fun acceptFriendRequest(fromUserId: String) {
        db.collection("FriendRequests")
            .whereEqualTo("fromUserId", fromUserId)
            .whereEqualTo("toUserId", user.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) return@addOnSuccessListener

                val docId = querySnapshot.documents.first().id

                db.collection("FriendRequests").document(docId).delete()
                    .addOnSuccessListener {
                        // Add both users to each other's friends list
                        val currentUserRef = db.collection("UserProfiles").document(user.uid)
                            .collection("Friends").document(fromUserId)
                        val otherUserRef = db.collection("UserProfiles").document(fromUserId)
                            .collection("Friends").document(user.uid)

                        val data = mapOf("userId" to fromUserId)
                        val reverseData = mapOf("userId" to user.uid)

                        currentUserRef.set(data)
                            .continueWithTask {
                                otherUserRef.set(reverseData)
                            }
                            .addOnSuccessListener {
                                getFriendProfiles()
                                getFriendRequests()
                            }
                    }
            }
    }

    fun rejectFriendRequest(fromUserId: String) {
        db.collection("FriendRequests")
            .whereEqualTo("fromUserId", fromUserId)
            .whereEqualTo("toUserId", user.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) return@addOnSuccessListener

                val docId = querySnapshot.documents.first().id

                db.collection("FriendRequests").document(docId).delete()
                    .addOnSuccessListener {
                        getFriendRequests()
                    }
            }
    }
}
