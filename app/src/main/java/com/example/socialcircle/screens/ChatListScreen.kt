package com.example.socialcircle.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.socialcircle.models.ChatListItem
import com.example.socialcircle.viewModels.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun ChatListScreen(viewModel: ChatViewModel = viewModel()) {

    LaunchedEffect(Unit) {
        viewModel.getChatList()
    }

    val chatList by viewModel.chatList.collectAsState()
    Log.d("mine", "chat size: ${chatList.size}")
    LazyColumn {
        items(chatList) { chat ->
            ChatItemView(
                chat
            ){
                //Todo ChatScreen(viewModel, chat.otherUserId)
            }
        }
    }
}


@Composable
fun ChatItemView(
    chat: ChatListItem,
    onChatClick: (ChatListItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChatClick(chat) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = chat.profileImageUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = chat.name, fontWeight = FontWeight.Bold)
            Text(
                text = chat.lastMessage,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(chat.lastMessageTimestamp!!.toDate()),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )

            if (chat.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(Color.Red, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = chat.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
