package com.example.socialcircle.viewModels

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialcircle.models.ChatListItem
import com.example.socialcircle.models.ChatMessage
import com.example.socialcircle.models.Chats
import com.example.socialcircle.models.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.getField
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class ChatViewModel: ViewModel() {

    private val _user = FirebaseAuth.getInstance().currentUser!!
    private val _otherUser = mutableStateOf<UserProfile?>(null)
    private val db = Firebase.firestore
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _chatList = MutableStateFlow<List<ChatListItem>>(emptyList())
    private lateinit var chatId: String
    private var _isChatExist = false
    private val _isFriends = mutableStateOf(false)
    private var oldestMessage: DocumentSnapshot? = null
    private val messageLimit: Long = 20
    private var isLoading = false
    private var allMessagesLoaded = false
    private var messageListener: ListenerRegistration? = null
    private var otherUserListener: ListenerRegistration? = null
    private var otherUserChatListener: ListenerRegistration? = null
    private val _unread = mutableIntStateOf(0)
    private var newMessage = true

    val messages = _messages.asStateFlow()
    val chatList = _chatList.asStateFlow()

    val user = _user
    val isFriends = _isFriends
    val otherUser = _otherUser
    val unread = _unread

    init{
        deleteExpiredChats()
    }

    fun resetRead(){
        if(newMessage) {
            db.collection("UserProfiles")
                .document(_user.uid)
                .collection("Chats")
                .document(chatId)
                .update("unreadCount", 0)
            newMessage = false
        }
    }

    fun getOtherUserListeners(otherUserId: String){
        otherUserListener = db.collection("UserProfiles")
            .document(otherUserId)
            .addSnapshotListener { snapshot, error ->
                if(error != null){
                    return@addSnapshotListener
                }

                otherUser.value = snapshot?.toObject(UserProfile::class.java)
            }

        otherUserChatListener = db.collection("UserProfiles")
            .document(otherUserId)
            .collection("Chats")
            .document(chatId)
            .addSnapshotListener { snapshot, error ->
                if(error != null){
                    return@addSnapshotListener
                }

                val unread = snapshot?.getField<Int>("unreadCount")

                _unread.intValue = unread!!
            }
    }

    fun removeUserListener(){
        otherUserListener?.remove()
        otherUserListener = null
    }

    fun getChatId(user2: String) {
        chatId = listOf(user.uid, user2).sorted().joinToString("::")
    }

    fun sendMessage(receiverId: String, text: String){
        viewModelScope.launch {
            if (!_isChatExist) {
                createChatSession(receiverId)
                _isChatExist = true
            }
            db.collection("Chats")
                .document(chatId)
                .collection("messages")
                .add(
                    ChatMessage(
                        senderId = _user.uid,
                        text = text,
                        timestamp = Timestamp.now()
                    )
                )

            db.collection("UserProfiles")
                .document(user.uid)
                .collection("Chats")
                .document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to text,
                        "lastMessageTimestamp" to Timestamp.now()
                    )
                )

            db.collection("UserProfiles")
                .document(receiverId)
                .collection("Chats")
                .document(chatId)
                .update(
                    "lastMessage", text,
                    "lastMessageTimestamp", Timestamp.now(),
                    "unreadCount", FieldValue.increment(1)
                )
        }
    }

    fun loadOldMessages(isInitialLoading: Boolean) {
        if (isInitialLoading) {
            allMessagesLoaded = false
            oldestMessage = null
        }

        if (isLoading || allMessagesLoaded) {
            return
        }

        isLoading = true

        var query = db.collection("Chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(messageLimit)

        if (!isInitialLoading) {
            if (oldestMessage != null) {
                query = query.startAfter(oldestMessage!!)
            }
        }

        query.get()
            .addOnSuccessListener { snapshot ->
                isLoading = false

                val olderMessages = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)
                }

                if (isInitialLoading) {
                    _messages.value = olderMessages
                } else {
                    _messages.value = _messages.value + olderMessages
                }

                oldestMessage = snapshot.documents.lastOrNull()

                if (olderMessages.isEmpty() && !isInitialLoading) {
                    allMessagesLoaded = true
                } else if (snapshot.documents.size < messageLimit && !olderMessages.isEmpty()) {
                    allMessagesLoaded = true
                }

            }
            .addOnFailureListener { exception ->
                isLoading = false
            }
    }

    fun listenForMessages() {
        val query = db.collection("Chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .startAfter(Timestamp.now())

        query.addSnapshotListener {snapshot, error->
            if(error != null || snapshot == null){
                return@addSnapshotListener
            }
            val newMessages = snapshot.documentChanges.mapNotNull { doc ->
                doc.document.toObject(ChatMessage::class.java)
            }
            _messages.value = newMessages + _messages.value
            newMessage = true
        }
    }

    fun removeMessageListener(){
        messageListener?.let{
            it.remove()
            messageListener = null
        }
    }

    fun getChatList(){
        db.collection("UserProfiles")
            .document(user.uid)
            .collection("Chats")
            .addSnapshotListener { snapshots, error ->
                if(error != null || snapshots == null){
                    return@addSnapshotListener
                }
                val chatIds = snapshots.documents.mapNotNull{doc->
                    doc.toObject(ChatListItem::class.java)
                }
                _chatList.value = chatIds
            }
    }

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

        val time = Timestamp.now()


        val addUser1Chat = async {
            db.collection("UserProfiles")
                .document(user.uid)
                .collection("Chats")
                .document(chatId)
                .set(
                    ChatListItem(
                        chatId = chatId,
                        otherUserId = otherUserId,
                        profileImageUrl = user2Snapshot.getString("photoUrl")!!,
                        name = user2Snapshot.getString("name")!!,
                        isTemporary = !_isFriends.value,
                        expireAt = time
                    )
                )
        }

        val addUser2Chat = async {
            db.collection("UserProfiles")
                .document(otherUserId)
                .collection("Chats")
                .document(chatId)
                .set(
                    ChatListItem(
                        chatId = chatId,
                        otherUserId = user.uid,
                        profileImageUrl = user1Snapshot.getString("photoUrl")!!,
                        name = user1Snapshot.getString("name")!!,
                        isTemporary = !_isFriends.value,
                        expireAt = time
                    )
                )
        }

        val chat = async{
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.HOUR_OF_DAY,  12)
            db.collection("Chats")
                .document(chatId)
                .set(
                    Chats(
                        chatId = chatId,
                        startsAt = time
                    )
                )
        }

        addUser1Chat.await()
        addUser2Chat.await()
        chat.await()
    }

    fun isChatExists() {
        db.collection("Chats")
            .document(chatId)
            .get()
            .addOnSuccessListener {chatSnapshot->
                _isChatExist = chatSnapshot.exists()
            }
    }

    fun isFriendsCheck(otherUserId: String){
        db.collection("UserProfiles")
            .document(user.uid)
            .collection("Friends")
            .document(otherUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                _isFriends.value = snapshot.exists()
            }
    }

    fun deleteExpiredChats(){
        db.collection("UserProfiles")
            .document(_user.uid)
            .collection("Chats")
            .get()
            .addOnSuccessListener { documentSnapshots ->
                Log.d("mine", "deleting expired chats ${documentSnapshots.size()}")
                documentSnapshots.forEach { snapshot ->
                    Log.d("mine", "this one ${snapshot.getBoolean("temporary")}")
                    if(snapshot.getBoolean("temporary") == true && snapshot.getTimestamp("expireAt")!! < Timestamp.now()){
                        deleteChat(snapshot.getString("chatId")!!, snapshot.getString("otherUserId")!!)
                    }
                }
            }
    }

    private fun deleteChat(documentId: String, otherUserId: String){
        db.collection("UserProfiles")
            .document(_user.uid)
            .collection("Chats")
            .document(documentId)
            .delete()

        db.collection("UserProfiles")
            .document(otherUserId)
            .collection("Chats")
            .document(documentId)
            .delete()

        db.collection("Chats")
            .document(documentId)
            .delete()
    }
}
