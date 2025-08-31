package com.example.socialcircle.screens.profileSetup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.socialcircle.navigation.AppScreens
import com.example.socialcircle.viewModels.ProfileSetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetContactScreen(
    viewModel: ProfileSetupViewModel,
    navController: NavController
) {
    val user = viewModel.user
    var phone by remember { mutableStateOf(user.value.phoneNumber) }
    var isLoading by remember { mutableStateOf(false) }
    var isWrong by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Enter Phone Number") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "What's your mobile number?",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter the mobile number on which you can be contacted. No one will see this on your profile.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    // allow only digits and +
                    if (input.all { it.isDigit() || it == '+' }) {
                        phone = input
                    }
                },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            if(isWrong){
                Text("Your phone number is wrong.", color = Color.Red)
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                        isWrong = !viewModel.updatePhoneNumber(phone)
                        if(!isWrong){
                            isLoading = true
                            navController.navigate(AppScreens.GetProfilePic.route)
                        }
                    },
                    enabled = phone.length >= 10 && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
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
