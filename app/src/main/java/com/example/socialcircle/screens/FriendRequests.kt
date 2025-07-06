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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.socialcircle.ui.theme.Blue20
import com.example.socialcircle.viewModels.FriendsViewModel

@Composable
fun FriendsRequest(
    friendsViewModel: FriendsViewModel
) {
    SideEffect {
        friendsViewModel.getFriendRequests()
    }

    val users by friendsViewModel.requestList.collectAsState()

    if(users.isEmpty()){
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()){
            Text("No Pending Requests", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }

    else {
        LazyColumn {
            items(users) { profile ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {  }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(Color(0xFFB3E5FC),  Color(0xFF40C4FF), Color(0xFF0091EA))
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
                        Text(
                            text = profile.userName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Button(
                        onClick = { friendsViewModel.acceptFriendRequest(profile.uid) },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue20,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Accept", style = MaterialTheme.typography.bodyMedium)
                        Icon(Icons.Default.PersonAdd, contentDescription = "Accept Friend Request", tint = Color.White)
                    }

                    IconButton(
                        onClick = { friendsViewModel.rejectFriendRequest(profile.uid) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                        ),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel Request")
                    }
                }
                HorizontalDivider()
            }
        }
    }
}