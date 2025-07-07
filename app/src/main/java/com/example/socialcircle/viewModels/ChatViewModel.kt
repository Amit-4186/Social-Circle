package com.example.socialcircle.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialcircle.models.ChatListItem
import com.example.socialcircle.models.ChatMessage
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class ChatViewModel: ViewModel() {

    private val _user = FirebaseAuth.getInstance().currentUser!!
    private val db = Firebase.firestore
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _chatList = MutableStateFlow<List<ChatListItem>>(emptyList())
    private lateinit var chatId: String
    private var isChatExist = false

    val messages = _messages.asStateFlow()
    val chatList = _chatList.asStateFlow()
    val user = _user

    fun getChatId(user2: String) {
        chatId = listOf(user.uid, user2).sorted().joinToString("::")
        Log.d("mine", chatId)
    }

    fun sendMessage(receiverId: String, text: String){
        Log.d("mine", "ischatExist: $isChatExist $chatId")
        viewModelScope.launch {
            if (!isChatExist) {
                createChatSession(receiverId)
                isChatExist = true
            }
            db.collection("Chats")
                .document(chatId)
                .collection("messages")
                .add(
                    ChatMessage(
                        senderId = _user.uid,
                        receiverId = receiverId,
                        text = text,
                        timestamp = Timestamp.now()
                    )
                )
        }
    }

    fun listenForMessages(){

        db.collection("Chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->

                if(error != null){
                    Log.e("mine","error in chatListener")
                    return@addSnapshotListener
                }

                snapshot?.let{
                    Log.d("mine", "received chat ${snapshot.documents.size}")
                    val list = it.documents.mapNotNull { doc->
                        try {
                            doc.toObject(ChatMessage::class.java)
                        }
                        catch(e: Exception){
                            Log.e("mine", "${e.message}")
                            ChatMessage(text = "notFound")
                        }
                    }

                    _messages.value = list
                }
            }
    }

    fun getChatList(){
        db.collection("UserProfiles")
            .document(user.uid)
            .collection("Chats")
            .get()
            .addOnSuccessListener { snapshots ->
                val chatIds = snapshots.documents.mapNotNull{doc->
                    doc.toObject(ChatListItem::class.java)
                }
                _chatList.value = chatIds
            }
    }

//    private fun createChatSession(otherUserId: String){
//        val calendar = Calendar.getInstance()
//        calendar.add(Calendar.HOUR_OF_DAY, 12)
//
//        db.collection("UserProfiles")
//            .document(user.uid)
//            .get()
//            .addOnSuccessListener { user1 ->
//                db.collection("UserProfiles")
//                    .document(otherUserId)
//                    .get()
//                    .addOnSuccessListener {user2 ->
//                        db.collection("UserProfiles")
//                            .document(user.uid)
//                            .collection("Chats")
//                            .add(
//                                ChatListItem(
//                                    chatId = chatId,
//                                    otherUserId = otherUserId,
//                                    profileImageUrl = user1.getString("photoUrl")!!,
//                                    name = user1.getString("name")!!
//                                )
//                            )
//
//                        db.collection("UserProfiles")
//                            .document(otherUserId)
//                            .get()
//                            .addOnSuccessListener {
//                                ChatListItem(
//                                    chatId = otherUserId,
//                                    otherUserId = user.uid,
//                                    profileImageUrl = user2.getString("photoUrl")!!,
//                                    name = user2.getString("name")!!
//                                )
//                            }
//                    }
//            }
//    }

    suspend fun createChatSession(otherUserId: String) = coroutineScope{
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, 12)

        val user1Snapshot = db.collection("UserProfiles")
            .document(user.uid)
            .get()
            .await()

        val user2Snapshot = db.collection("UserProfiles")
            .document(otherUserId)
            .get()
            .await()

        val addUser1Chat = async {
            db.collection("UserProfiles")
                .document(user.uid)
                .collection("Chats")
                .add(
                    ChatListItem(
                        chatId = chatId,
                        otherUserId = otherUserId,
                        profileImageUrl = user2Snapshot.getString("photoUrl")!!,
                        name = user2Snapshot.getString("name")!!
                    )
                )
        }

        val addUser2Chat = async {
            db.collection("UserProfiles")
                .document(otherUserId)
                .collection("Chats")
                .add(
                    ChatListItem(
                        chatId = chatId,
                        otherUserId = user.uid,
                        profileImageUrl = user1Snapshot.getString("photoUrl")!!,
                        name = user1Snapshot.getString("name")!!
                    )
                )
        }

        addUser1Chat.await()
        addUser2Chat.await()
    }

    suspend fun isChatExists() {
        val chatSnapshot = db.collection("Chats")
            .document(chatId)
            .get()
            .await()

        isChatExist = chatSnapshot.exists()
    }
}