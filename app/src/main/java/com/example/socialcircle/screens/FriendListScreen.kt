package com.example.socialcircle.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.socialcircle.models.ProfileDetails
import com.example.socialcircle.viewModels.FriendsViewModel



@Composable
fun FriendListScreen(viewModel: FriendsViewModel = viewModel()) {

    val friends by viewModel.friendList.collectAsState()

    LazyColumn(modifier = Modifier.padding(24.dp)) {
        items(friends) { friend ->
//            Row(modifier = Modifier.padding(8.dp)) {
//                AsyncImage(
//                    friend.photoUrl,
//                    "profile picture",
//                    modifier = Modifier
//                        .size(48.dp)
//                        .padding(end = 8.dp)
//                        .clip(CircleShape),
//                    contentScale = ContentScale.Crop
//                )
//                Text(friend.name)
//            }
            FollowerItem(friend) { }
        }
    }
}

@Composable
fun FollowerItem(
    profile: ProfileDetails,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture with Story-like ring
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFB3E5FC),  Color(0xFF40C4FF), Color(0xFF0091EA))  //Color.Magenta, Color.Blue, Color.White)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = profile.photoUrl,
                contentDescription = "${profile.name}'s profile picture",
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            profile.userName?.let {
                Text(
                    text = "@$it",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Stylish Follow Button
        Button(
            onClick = { /* Handle follow/unfollow */ },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0085FF),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text("Follow", style = MaterialTheme.typography.bodyMedium)
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
