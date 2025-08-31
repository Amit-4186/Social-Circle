package com.example.socialcircle.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.socialcircle.R
import com.example.socialcircle.models.ChatMessage
import com.example.socialcircle.models.UserStatus
import com.example.socialcircle.ui.theme.Blue10
import com.example.socialcircle.ui.theme.Blue20
import com.example.socialcircle.ui.theme.Gray10
import com.example.socialcircle.viewModels.ChatViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = viewModel(),
    otherUserId: String
) {
    LaunchedEffect(Unit) {
        launch {
            chatViewModel.getOtherUserListeners(otherUserId)
        }
        chatViewModel.getChatId(otherUserId)
        chatViewModel.isChatExists()
        chatViewModel.isFriendsCheck(otherUserId)
        chatViewModel.loadOldMessages(true)
        chatViewModel.listenForMessages()
//        chatViewModel.resetRead()
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("mine", "destroyed")
            chatViewModel.removeMessageListener()
            chatViewModel.removeUserListener()
        }
    }

    val scrollState = rememberScrollState()
    val textLayoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val isScrollable = remember(textLayoutResult.value) {
        val lineCount = textLayoutResult.value?.lineCount ?: 0
        lineCount > 5
    }

    val isFriends = chatViewModel.isFriends.value
    var showNotes by remember { mutableStateOf(false) }

    val messages by chatViewModel.messages.collectAsState()
    val currentUserId = chatViewModel.user.uid
    var text by remember { mutableStateOf("") }
    val otherUser = chatViewModel.otherUser
    val unread = chatViewModel.unread.intValue

    val listState = rememberLazyListState()
    val isAtLatestMessage by remember {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset

            firstIndex == 0 && firstOffset < 20
        }
    }
    LaunchedEffect(isAtLatestMessage, messages) {
        if (isAtLatestMessage && messages.isNotEmpty()) {
            chatViewModel.resetRead()
        }
    }

    Scaffold(
        Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, "back")
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = otherUser.value?.photoUrl ?: "",
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = otherUser.value?.name ?: "",
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                lineHeight = 22.sp
                            )
                            Text(
                                text = otherUser.value?.status ?: UserStatus.Offline.name,
                                fontSize = 12.sp,
                                color = if((otherUser.value?.status ?: UserStatus.Offline.name) == UserStatus.Online.name) Color.White else Color.LightGray,
                                lineHeight = 12.sp
                            )
                        }
                    }
                },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = {
                        expanded = true
                    }) {
                        Icon(Icons.Default.MoreVert, "Options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Chat") },
                            onClick = { expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Block") },
                            onClick = { expanded = false }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue20,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )

        }
    ) { innerPadding ->
        Box {
            Image(
                painterResource(R.drawable.doodle), "background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (!isFriends && showNotes) {
                    NoteMessageBubble { showNotes = false }
                }
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    reverseLayout = true,
                    state = listState
                ) {
                    items(messages.size) { index ->
                        if(index == unread){
                            SeenDivider()
                        }
                        MessageBubble(
                            messages[index],
                            isCurrentUser = messages[index].senderId == currentUserId
                        )

                        if (index == messages.lastIndex) {
                            chatViewModel.loadOldMessages(false)
                        }
                    }

                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                        .fillMaxWidth()
                        .imePadding()
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        shape = RoundedCornerShape(30.dp),
                        placeholder = { Text("Message") },
                        maxLines = 4,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .verticalScroll(if (isScrollable) scrollState else ScrollState(0)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue20,
                            focusedContainerColor = Color.White,
                            errorContainerColor = Color.White,
                            disabledContainerColor = Color.Gray,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                chatViewModel.sendMessage(otherUserId, text)
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
    }
}


@Composable
fun MessageBubble(message: ChatMessage, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 100.dp, max = 290.dp)
                .background(
                    if (isCurrentUser) Blue10 else Gray10,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(text = message.text, color = Color.DarkGray)
        }
    }
}

@Composable
fun NoteMessageBubble(removeNote: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
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
                text = "x", fontSize = 18.sp, color = Blue20, modifier = Modifier
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

@Composable
fun SeenDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = DividerDefaults.Thickness, color = Color.Gray
        )
        Text(
            text = "Seen",
            modifier = Modifier.padding(horizontal = 8.dp),
            color = Color.Gray,
            fontSize = 12.sp
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = DividerDefaults.Thickness, color = Color.Gray
        )
    }
}