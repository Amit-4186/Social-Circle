package com.example.socialcircle.screens.profileSetup

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.socialcircle.navigation.AppScreens
import com.example.socialcircle.viewModels.ProfileSetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePictureScreen(
    viewModel: ProfileSetupViewModel,
    navController: NavController
) {
    val user = viewModel.user
    val placeholder = "https://cdn-icons-png.flaticon.com/512/149/149071.png"
    var isLoading by remember { mutableStateOf(false) }
    var pictureUri by remember { mutableStateOf<String?>(user.value.photoUrl) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        pictureUri = uri?.toString()
        Log.d("mine", "$uri")
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Choose Profile Picture") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Add profile picture",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Left
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Add a profile picture so that your friends know it's you. Everyone will be able to see your picture",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = rememberAsyncImagePainter(pictureUri ?: placeholder),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .clickable {
                        // TODO: image picker
                        launcher.launch("image/*")
                    }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    enabled = !isLoading
                ) {
                    Text("Back")
                }

                Button(
                    onClick = {
                        pictureUri?.let {
                            isLoading = true
                            viewModel.updateProfilePic(it)
                            navController.navigate(AppScreens.GetUserName.route)
                        }
                    },
                    enabled = !isLoading && pictureUri != null, // lock button
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text("Next")
                    }
                }
            }
        }
    }
}
