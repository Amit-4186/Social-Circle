package com.example.socialcircle.screens

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.socialcircle.R
import com.example.socialcircle.viewModels.ProfileSetupViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.socialcircle.navigation.AppScreens

private sealed class DetailScreens(val title: String, val subtitle: String, val entry: String){
    object PhoneNo: DetailScreens(
        "What's your mobile number?",
        "Enter the mobile number on which you can be contacted. No one will see this on your profile.",
        "Mobile number"
        )
    object Name: DetailScreens(
        "What's your name?",
        "",
        "Full Name"
    )
    object UserName: DetailScreens(
        "Create a userName",
        "Add a unike username for the account.",
        "Username"
    )
    object DateOfBirth: DetailScreens(
        "What's your date of birth?",
        "Use your own date of birth even if this account is for a pet or something else. No one can see it unless you choose to share it",
        "Birth date"
    )
    object ProfilePic: DetailScreens(
        "Add profile picture",
        "Add a profile picture so that your friends know it's you. Everyone will be able to see your picture",
        "Profile Picture"
    )

    companion object{
        val steps = listOf(PhoneNo, Name, UserName, DateOfBirth, ProfilePic)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(navController: NavController){
    val viewModel: ProfileSetupViewModel = viewModel()
    val context = LocalContext.current
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = DetailScreens.steps[currentStepIndex]
    val user by viewModel.user.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if(currentStepIndex > 0){
                        IconButton(
                            onClick = {
                                currentStepIndex--
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                        }
                    }
                },
                title = {}
            )
        }
    ) {innerPadding->
        DetailEntry(
            Modifier.padding(innerPadding),
            context,
            currentStep.title,
            currentStep.subtitle,
            currentStep.entry
        ){data->
            var isNext = true
            if(data == ""){
                Toast.makeText(context, "Please fill the entry.", Toast.LENGTH_SHORT).show()
                isNext = false
            }
            else {
                when (currentStep) {
                    DetailScreens.DateOfBirth -> {
                        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                        val date: Date = sdf.parse(data)!!
                        val timestamp = Timestamp(date)
                        viewModel.updateBirthDate(timestamp)
                    }

                    DetailScreens.Name -> viewModel.updateName(data)
                    DetailScreens.PhoneNo ->{
                        if(!viewModel.updatePhoneNumber(data)){
                            Toast.makeText(context, "Phone no is incorrect", Toast.LENGTH_SHORT).show()
                            isNext = false
                        }
                    }
                    DetailScreens.ProfilePic -> {
                        Log.d("mine", "profile")
                        viewModel.profilePic = data.toUri()
                        viewModel.uploadOnFireStore()
                    }

                    DetailScreens.UserName -> {
                        isNext = false
                        viewModel.checkIfUsernameExists(data){ isExist->
                            if(isExist){
                                Toast.makeText(context, "Username already exists.", Toast.LENGTH_SHORT).show()
                            }
                            else{
                                viewModel.updateUsername(data)
                                currentStepIndex++
                            }
                        }
                    }
                }
            }
            if (currentStepIndex < DetailScreens.steps.size - 1) {
                if(isNext) currentStepIndex++
            }
            else{
                navController.navigate(AppScreens.RootNav.route)
            }
            Log.d("mine", "$user")
        }
    }
}



@Composable
private fun DetailEntry(
    modifier: Modifier,
    context: Context,
    title: String = "",
    subtitle: String = "",
    entry: String = "",
    onNextClick: (String)->Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // TextField for mobile number
        var data by remember{mutableStateOf("")}

        when(entry){
            "Birth date" ->{
                data = "2000/1/1"
                val datePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        data = "$year/${month+1}/$day"
                    },
                    2000,
                    0,
                    1
                )

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        datePickerDialog.show()
                    }
                ) {
                    Text(data)
                }
            }
            "Profile Picture"->{

                var pictureUri by remember { mutableStateOf<Uri?>(null) }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    pictureUri = uri
                    data = uri.toString()
                }


                Image(
                    if (pictureUri != null) {
                        rememberAsyncImagePainter(model = pictureUri)
                    } else {
                        painterResource(R.drawable.profile_loading)
                    },
                    "",
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        launcher.launch("image/*")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Add picture")
                }
            }
            else->{
                OutlinedTextField(
                    value = data,
                    onValueChange = { data = it },
                    label = { Text(entry) },
                    keyboardOptions = if(entry == "Mobile number"){ KeyboardOptions(keyboardType = KeyboardType.Phone)}
                    else{ KeyboardOptions(keyboardType = KeyboardType.Text)},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Next Button
        Button(
            onClick = {
                onNextClick(data)
                data = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
        ) {
            Text("Next")
        }
    }
}