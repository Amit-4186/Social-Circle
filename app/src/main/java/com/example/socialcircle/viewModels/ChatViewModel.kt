package com.example.socialcircle.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.socialcircle.models.ChatListItem
import com.example.socialcircle.models.ChatMessage
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel: ViewModel() {

    private val _user = FirebaseAuth.getInstance().currentUser!!
    private val db = Firebase.firestore
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _chatList = MutableStateFlow<List<ChatListItem>>(emptyList())

    val messages = _messages.asStateFlow()
    val chatList = _chatList.asStateFlow()
    val user = _user

    private fun getChatId(user1: String, user2: String): String {
        return listOf(user1, user2).sorted().joinToString("::")
    }

    fun sendMessage(receiverId: String, text: String){
        db.collection("chats")
            .document(getChatId(_user.uid, receiverId))
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

    fun listenForMessages(senderId: String){
        val chatId = getChatId(_user.uid, senderId)

        db.collection("chats")
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
            .collection("chats")
            .get()
            .addOnSuccessListener { snapshots ->
                val chatIds = snapshots.documents.mapNotNull{doc->
                    doc.toObject(ChatListItem::class.java)
                }
                _chatList.value = chatIds
            }
    }
}