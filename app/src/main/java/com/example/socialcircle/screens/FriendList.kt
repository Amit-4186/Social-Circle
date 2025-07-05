package com.example.socialcircle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.socialcircle.viewModels.FriendsViewModel


@Composable
fun FriendList(friendsViewModel: FriendsViewModel = viewModel(), onChatClick: (String) -> Unit) {

    SideEffect {
        friendsViewModel.getFriendProfiles()
    }

    val friends by friendsViewModel.friendList.collectAsState()

    LazyColumn( modifier = Modifier.fillMaxSize() ) {
        items(friends) { friend ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
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
                        contentDescription = "${friend.name}'s profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }

                Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
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
                    onClick = { friendsViewModel },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray
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
    }
}

//@Composable
//fun FollowerItem(
//    profile: ProfileDetails,
//    onClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() }
//            .padding(12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Circular profile image
//        AsyncImage(
//            model = profile.photoUrl,
//            contentDescription = "Profile Picture",
//            modifier = Modifier
//                .size(56.dp)
//                .clip(CircleShape)
//        )
//
//        Spacer(modifier = Modifier.width(12.dp))
//
//        Column(modifier = Modifier.weight(1f)) {
//            Text(
//                text = profile.name,
//                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
//            )
//            Text(
//                text = profile.userName, // Optional username/description
//                style = MaterialTheme.typography.bodySmall,
//                color = Color.Gray
//            )
//        }
//
//        // Optional "Follow" / "Following" button
//        TextButton(
//            onClick = { /* Handle follow/unfollow */ },
//            modifier = Modifier
//                .height(36.dp)
//        ) {
//            Text("Following") // or "Follow"
//        }
//    }
//}
