package com.example.socialcircle.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.socialcircle.AuthResultState
import com.example.socialcircle.AuthenticationViewModel
import com.example.socialcircle.Screen

@Composable
fun LoginScreen(viewModel: AuthenticationViewModel, navController: NavController) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthResultState.Success -> {
                Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Main.route) {
                    popUpTo(0) { inclusive = true }
                }
            }

            is AuthResultState.Error -> {
                Toast.makeText(context, (authState as AuthResultState.Error).error, Toast.LENGTH_SHORT).show()
            }

//            AuthResultState.EmailVerified -> {
//                navController.navigate(Screen.Main.route){
//                    popUpTo(0) { inclusive= true }
//                }
//            }

            AuthResultState.EmailNotVerified -> {
                navController.navigate(Screen.Verification.route)
            }

            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isLoginMode) "Login" else "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        if (!isLoginMode) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login / Signup Button
        Button(
            onClick = {
                if (isLoginMode) {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        viewModel.loginWithEmail(email, password)
                    } else {
                        Toast.makeText(
                            context,
                            "Please enter email and/or password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        if (password == confirmPassword) {
                            viewModel.signupWithEmail(email, password)
                            Toast.makeText(context, "Verification email sent", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screen.Verification.route)
                        } else {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Please enter email and/or password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoginMode) "Login" else "Sign Up")
        }

        Spacer(modifier = Modifier.height(4.dp))

        //Forgot Password
        if (isLoginMode) {
            TextButton(
                onClick = { navController.navigate("forgotPassword") }
            ) { Text("Forgot Password?") }
        }

        //Switch Login / SignUp
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if(isLoginMode) "Don't have an account?" else "Already have an account?")
            TextButton(onClick = { isLoginMode = !isLoginMode }) {
                Text(if (isLoginMode) "Sign Up" else "Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (authState) {
            is AuthResultState.Success -> Text((authState as AuthResultState.Success).message)
            is AuthResultState.Error -> Text(
                (authState as AuthResultState.Error).error ?: "An error occurred",
                color = MaterialTheme.colorScheme.error
            )

//            AuthResultState.VerificationEmailSent -> Text("Check your inbox for a verification link.")
//            AuthResultState.EmailNotVerified -> {
//                Text(text = "Email not verified. Please verify to continue.", textAlign = TextAlign.Center , modifier = Modifier.fillMaxWidth())
//
//                Button(onClick = { viewModel.checkEmailVerification() }) {
//                    Text("Check Verification Status")
//                }
//
//                Spacer(Modifier.height(8.dp))
//
//                TextButton(onClick = { viewModel.resendVerificationEmail() }) {
//                    Text("Resend Verification Email")
//                }
//            }

//            AuthResultState.EmailVerified -> Text("Email verified! You can now log in.")
            else -> {}
        }
    }
}
