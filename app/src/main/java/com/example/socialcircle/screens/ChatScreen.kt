package com.example.socialcircle.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialcircle.models.ChatMessage
import com.example.socialcircle.viewModels.ChatViewModel


@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    otherUserId: String
) {
    LaunchedEffect(Unit) {
        Log.d("mine", "started $otherUserId")
        viewModel.getChatId(otherUserId)
        viewModel.isChatExists()
        viewModel.isFriendsCheck(otherUserId)
        viewModel.listenForMessages()
    }

    val isFriends = viewModel.isFriends.value

    if(isFriends) {
        NoteMessageBubble("You're in a temporary chat. Messages here will disappear 12 hours after they're created.")
    }
    val messages by viewModel.messages.collectAsState()
    val currentUserId = viewModel.user.uid
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                MessageBubble(message, isCurrentUser = message.senderId == currentUserId)
            }
        }

        Row(modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                if (text.isNotBlank()) {
                    viewModel.sendMessage( otherUserId, text)
                    text = ""
                }
            }) {
                Text("Send")
            }
        }
    }
}


@Composable
fun MessageBubble(message: ChatMessage, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isCurrentUser) Color(0xFFDCF8C6) else Color(0xFFEDEDED),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            Text(text = message.text)
        }
    }
}

@Composable
fun NoteMessageBubble(note: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = Color(0xFFD9FDD3), // WhatsApp greenish bubble
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = "Note",
                color = Color(0xFF075E54),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}

