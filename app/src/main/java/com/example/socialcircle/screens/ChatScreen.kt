package com.example.socialcircle.screens

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialcircle.models.ChatMessage
import com.example.socialcircle.ui.theme.Blue10
import com.example.socialcircle.ui.theme.Blue20
import com.example.socialcircle.ui.theme.Gray10
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

    val scrollState = rememberScrollState()
    val textLayoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val isScrollable = remember(textLayoutResult.value) {
        val lineCount = textLayoutResult.value?.lineCount ?: 0
        lineCount > 5
    }

    val isFriends = viewModel.isFriends.value
    var showNotes by remember { mutableStateOf(true) }

    val messages by viewModel.messages.collectAsState()
    val currentUserId = viewModel.user.uid
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .imePadding()) {
        if (!isFriends && showNotes) {
            NoteMessageBubble { showNotes = false }
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            reverseLayout = true
        ) {
            items(messages) { message ->
                MessageBubble(message, isCurrentUser = message.senderId == currentUserId)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                shape = RoundedCornerShape(30.dp),
                placeholder = { Text("Message") },
                maxLines = 4,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp)
                    .padding(4.dp)
                    .verticalScroll(if (isScrollable) scrollState else ScrollState(0)),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Default
                )
            )
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        viewModel.sendMessage(otherUserId, text)
                        text = ""
                    }
                },
                modifier = Modifier
                    .height(48.dp)
                    .aspectRatio(1f)
                    .background(Blue20, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
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
                .widthIn(min = 100.dp)
                .background(
                    if (isCurrentUser) Blue10 else Gray10,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(8.dp)
        ) {
            Text(text = message.text)
        }
    }
}

@Composable
fun NoteMessageBubble(removeNote: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .background(
                color = Blue10,
                shape = RoundedCornerShape(12.dp)
            )
            .border(0.5.dp, Blue20, RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Note",
                color = Blue20,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            Text(
                text = "x", fontSize = 18.sp, color = Blue20
                , modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 8.dp)
                    .clickable(onClick = removeNote)
            )
        }

        Text(
            text = """You're in a temporary chat. Messages will disappear after 12 hours from starting. Become friends to make it permanent""",
            fontSize = 14.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Justify,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(start = 8.dp, bottom = 8.dp, end = 8.dp)
                .fillMaxWidth()
        )
    }
}

