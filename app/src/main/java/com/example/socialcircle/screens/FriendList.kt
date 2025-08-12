package com.example.socialcircle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.socialcircle.R
import com.example.socialcircle.models.ProfileDetails
import com.example.socialcircle.navigation.MainScreens
import com.example.socialcircle.viewModels.FriendsViewModel


@Composable
fun FriendList(friendsViewModel: FriendsViewModel = viewModel(), onChatClick: (String) -> Unit, mainNavController: NavController) {

    SideEffect {
        friendsViewModel.getFriendProfiles()
    }

    val friends by friendsViewModel.friendList.collectAsState()

    if(friends.isEmpty()){
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text( "No Friends Yet", fontSize = 20.sp, fontWeight = FontWeight.SemiBold )
                TextButton(onClick = { mainNavController.navigate(MainScreens.Discover) { popUpTo(0) } }) {
                    Text("Discover People")
                }
            }
        }
    }

    else {
        LazyColumn( modifier = Modifier.fillMaxSize() ) {
            items(friends) { friend ->
                FriendItem(friend, {friendsViewModel.removeFriend(friend.uid)}) {
                    onChatClick(friend.uid)
                }
            }
        }
    }
}

@Composable
fun FriendItem(friend: ProfileDetails, onFriendRemove: () -> Unit, onChatClick: (String) -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = { onChatClick(friend.uid) }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0xFFB3E5FC),
                            Color(0xFF40C4FF),
                            Color(0xFF0091EA)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = friend.photoUrl,
                placeholder = painterResource(R.drawable.profile_loading),
                contentDescription = "${friend.name}'s profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }

        Column(modifier = Modifier
            .weight(1f)
            .padding(horizontal = 8.dp)) {
            Text(
                text = friend.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = friend.userName,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Button(
            onClick = { onFriendRemove() },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.LightGray
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Row {
                Text("Remove", style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.Default.PersonRemove, contentDescription = "Remove Friend")
            }
        }
    }
    HorizontalDivider()
}