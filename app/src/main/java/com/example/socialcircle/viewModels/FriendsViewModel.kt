package com.example.socialcircle.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.socialcircle.models.ProfileDetails
import com.example.socialcircle.models.RequestModel
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Calendar

class FriendsViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser!!
    private val _friendList = MutableStateFlow<List<ProfileDetails>>(emptyList())
    private val _requestList = MutableStateFlow<List<ProfileDetails>>(emptyList())

    val friendList = _friendList
    val requestList = _requestList

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

    private fun fetchFriendIds(onResult: (List<String>) -> Unit) {
        db.collection("UserProfiles").document(user.uid).collection("Friends")
            .get()
            .addOnSuccessListener { snapshot ->
                val friendIds =
                    snapshot.documents.mapNotNull { doc -> doc.getString("userId") }
                onResult(friendIds)
            }
    }

    private fun fetchFriendRequestIds(onResult: (List<String>) -> Unit) {
        Log.d("mine", "FetchFriendRequestIds called")
        db.collection("FriendRequests")
            .whereEqualTo("toUserId", user.uid)
            .get()
            .addOnSuccessListener { document ->
                val result = document.mapNotNull { doc ->
                    doc.getString("fromUserId")
                }
                Log.d("mine", "${document.size()} ${result.size}")
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

    fun sendFriendRequest(
        toUserId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        db.collection("FriendRequests")
            .add(
                RequestModel(
                    fromUserId = user.uid,
                    toUserId = toUserId,
                    timestamp = Timestamp.now()
                ),
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
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
                        val currentUserRef = db.collection("UserProfiles").document(user.uid)
                            .collection("Friends").document(fromUserId)
                        val otherUserRef = db.collection("UserProfiles").document(fromUserId)
                            .collection("Friends").document(user.uid)

                        val data = mapOf("userId" to fromUserId)
                        val reverseData = mapOf("userId" to user.uid)
                        val chatId = listOf(user.uid, fromUserId).sorted().joinToString("::")

                        currentUserRef.set(data)
                            .continueWithTask {
                                otherUserRef.set(reverseData)
                            }
                            .addOnSuccessListener {
                                getFriendProfiles()
                                getFriendRequests()
                            }

                        db.collection("Chats")
                            .document(chatId)
                            .get()
                            .addOnSuccessListener { querySnapshot->
                                if(querySnapshot.exists()){
                                    Log.d("mine", "changed chat to permanent")
                                    db.collection("UserProfiles").document(user.uid).collection("Chats")
                                        .document(chatId)
                                        .update("temporary", false)

                                    db.collection("UserProfiles").document(fromUserId).collection("Chats")
                                        .document(chatId)
                                        .update("temporary", false)
                                }
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

    fun removeFriend(
        friendUserId: String,
        onSuccess: () -> Unit = {},
        onFailure: (e: Exception) -> Unit = {}
    ) {
        Log.d("mine", "trying to remove")
        val currentUserRef = db.collection("UserProfiles")
            .document(user.uid)
            .collection("Friends")
            .document(friendUserId)
        val otherUserRef = db.collection("UserProfiles")
            .document(friendUserId)
            .collection("Friends")
            .document(user.uid)
        val chatId = listOf(user.uid, friendUserId).sorted().joinToString("::")
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, 12)

        val data = mapOf(
            "temporary" to true,
            "expireAt" to Timestamp(calendar.time)
        )

        val batch = db.batch()
        batch.delete(currentUserRef)
        batch.delete(otherUserRef)
        batch.update(
            db.collection("UserProfiles").document(user.uid).collection("Chats").document(chatId),
            data
            )
        batch.update(
            db.collection("UserProfiles").document(friendUserId).collection("Chats").document(chatId),
            data
        )

        batch.commit()
            .addOnSuccessListener {
                Log.d("mine","removed friend $friendUserId")
                getFriendProfiles()
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}
