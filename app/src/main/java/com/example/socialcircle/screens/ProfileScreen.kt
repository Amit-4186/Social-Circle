package com.example.socialcircle.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.socialcircle.AppScreens
import com.example.socialcircle.R
import com.example.socialcircle.viewModels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    mainNavController: NavController,
    appNavController: NavController,
    profileViewModel: ProfileViewModel
) {
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }
    val profile by profileViewModel.profileState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 8.dp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .wrapContentHeight()
                ) {
                    Text(
                        text = "Are you sure?",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Do you really want to log out?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        TextButton(
                            onClick = {
                                profileViewModel.logout()
                                appNavController.navigate(AppScreens.Login.route) { popUpTo(0) }
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Log Out")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        profile?.let { user ->
            AsyncImage(
                model = user.photoUrl,
                contentDescription = "Profile Picture",
                placeholder = painterResource(R.drawable.profile_loading),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            )
        } ?: Image(
            painterResource(R.drawable.profile_loading),
            contentDescription = "Empty Profile Pic",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = profile?.name ?: "...", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "@${profile?.userName ?: "..."}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

//        Text("${profile?.let { profile -> extractDateFromTimestamp(profile.birthDate!!) }}")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { if(profile != null) mainNavController.navigate(MainScreens.ProfileEdit.route) },
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, color = Color.Black),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Row {
                    Text(text = "Edit Profile")
                    Icon(
                        Icons.Default.Edit,
                        "edit profile",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(start = 8.dp)
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            OutlinedButton(
                onClick = { TODO() },
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, color = Color.Black),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Row {
                    Text(text = "Share Profile")
                    Icon(
                        Icons.Default.Share, "share profile",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(start = 8.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clickable(onClick = {}),
            shape = RoundedCornerShape(8.dp),
            tonalElevation = 6.dp,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, Color.LightGray),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(16.dp)
            ) {
                Text( text = "You have ${profile?.friendCount ?: "..."} friends", fontSize = 16.sp )
                IconButton(onClick = { mainNavController.navigate(MainScreens.Friend.route) }) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowForward,
                        "Friends",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = { showDialog = true }) {
            Text("Log Out", color = MaterialTheme.colorScheme.error)
        }
    }
}