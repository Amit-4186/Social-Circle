package com.example.socialcircle.screens

import android.net.Uri
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.socialcircle.R
import com.example.socialcircle.models.DateModel
import com.example.socialcircle.viewModels.ProfileViewModel
import com.google.firebase.Timestamp
import java.util.Calendar

@Composable
fun ProfileEditScreen(
    mainNavController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {

    val context = LocalContext.current
    val profile by viewModel.profileState.collectAsState()
    var photoUrl by remember { mutableStateOf(profile!!.photoUrl) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf(profile!!.name) }
    var birthDate by remember { mutableStateOf(profile!!.birthDate) }
    var birthDateModel by remember { mutableStateOf(extractDateFromTimestamp(birthDate!!)) }
    var phone by remember { mutableStateOf(profile!!.phoneNumber) }
    var username by remember { mutableStateOf(profile!!.userName) }
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedPhotoUri = uri
            }
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val imageModifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))

        Box(
            modifier = Modifier
                .size(100.dp)
                .aspectRatio(1f)
        ) {
            if (selectedPhotoUri == null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Picture",
                    placeholder = painterResource(R.drawable.profile_loading),
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier
                )
            } else {
                Image(
                    painter = rememberAsyncImagePainter(selectedPhotoUri),
                    contentDescription = "Selected Image",
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(30.dp)
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    )
                    .clickable(onClick = { imagePickerLauncher.launch("image/*") })
            ){
                Icon(Icons.Default.Edit, "Edit Profile Pic", modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Edit Profile", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name", fontSize = 12.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            shape = RoundedCornerShape(18.dp),
            maxLines = 1
        )

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(140.dp)) {
            Text("Date of Birth: ")
            AndroidView(
                factory = { context ->
                    NumberPicker(context).apply {
                        minValue = 1
                        maxValue = 31
                        value = birthDateModel.day
                        setOnValueChangedListener { _, _, newVal ->
                            birthDateModel = birthDateModel.copy(day = newVal)
                        }
                        scaleX = 0.8f
                        scaleY = 0.8f
                    }
                },
                modifier = Modifier.weight(1f)
            )

            AndroidView(
                factory = { context ->
                    NumberPicker(context).apply {
                        minValue = 1
                        maxValue = 12
                        value = birthDateModel.month
                        setOnValueChangedListener { _, _, newVal ->
                            birthDateModel = birthDateModel.copy(month = newVal)
                        }
                        scaleX = 0.8f
                        scaleY = 0.8f
                    }
                },
                modifier = Modifier.weight(1f)
            )

            AndroidView(
                factory = { context ->
                    NumberPicker(context).apply {
                        minValue = 1900
                        maxValue = Calendar.getInstance().get(Calendar.YEAR)
                        value = birthDateModel.year
                        setOnValueChangedListener { _, _, newVal ->
                            birthDateModel = birthDateModel.copy(year = newVal)
                        }
                        scaleX = 0.8f
                        scaleY = 0.8f
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number", fontSize = 12.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            shape = RoundedCornerShape(18.dp),
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username", fontSize = 12.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            shape = RoundedCornerShape(18.dp),
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(64.dp))
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (name != profile!!.name) viewModel.updateName(name)
                    if (birthDateModel != extractDateFromTimestamp(profile!!.birthDate!!)) viewModel.updateBirthDate(
                        birthDateModel
                    )
                    if (phone != profile!!.phoneNumber) viewModel.updatePhoneNumber(phone)
                    if (username != profile!!.userName) viewModel.updateUserName(username)
                    selectedPhotoUri?.let {
                        viewModel.updateProfilePic(it) { result ->
                            if (result) Toast.makeText(context, "Successful", Toast.LENGTH_SHORT)
                                .show()
                            else Toast.makeText(context, "Couldn't upload image", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                    mainNavController.popBackStack()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Save Changes")
            }

            OutlinedButton(
                onClick = { mainNavController.popBackStack() },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Cancel")
            }
        }
    }
}

fun extractDateFromTimestamp(timestamp: Timestamp): DateModel {
    val calendar = Calendar.getInstance().apply {
        time = timestamp.toDate()
    }

    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH) + 1
    val year = calendar.get(Calendar.YEAR)

    return DateModel(day, month, year)
}