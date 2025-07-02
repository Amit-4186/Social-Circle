package com.example.socialcircle.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.socialcircle.viewModels.AuthResultState
import com.example.socialcircle.viewModels.AuthenticationViewModel
import com.example.socialcircle.Screen

@Composable
fun VerificationScreen(viewModel: AuthenticationViewModel, navController: NavController) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        when (authState) {
            AuthResultState.EmailVerified -> {
                navController.navigate(Screen.ProfileSetup.route){   //Screen.ProfileCreation.route) {
                    popUpTo(0) { inclusive = true }
                }
            }

            AuthResultState.EmailNotVerified -> {
                Toast.makeText(context, "Email not verified", Toast.LENGTH_SHORT).show()
            }

            AuthResultState.VerificationEmailSent -> {
                Toast.makeText(context, "Verification email sent", Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(24.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = "Back"
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { viewModel.checkEmailVerification() }) {
                Text("Check Verification Status")
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = { viewModel.resendVerificationEmail() }) {
                Text("Resent verification Email")
            }

            Spacer(Modifier.height(8.dp))

            when(authState){
                is AuthResultState.Error -> {
                    val error = (authState as AuthResultState.Error).error
                    Text(error ?: "Unknown Error", color = MaterialTheme.colorScheme.error)
                }
                AuthResultState.VerificationEmailSent -> Text("(Check your Inbox)")
                else -> {}
            }
        }
    }
}