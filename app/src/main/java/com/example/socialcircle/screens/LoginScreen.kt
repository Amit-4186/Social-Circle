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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.socialcircle.AppScreens
import com.example.socialcircle.viewModels.AuthResultState
import com.example.socialcircle.viewModels.AuthenticationViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LastCharVisiblePasswordTransformation(
    private val lastCharVisible: Boolean
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text

        if (input.isEmpty()) return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        val masked = buildString {
            append("•".repeat(maxOf(0, input.length - 1)))
            append(if (lastCharVisible) input.last() else '•')
        }


        return TransformedText(AnnotatedString(masked), OffsetMapping.Identity)
    }
}



@Composable
fun LoginScreen(viewModel: AuthenticationViewModel, navController: NavController) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var lastCharVisible by remember { mutableStateOf(false) }
    var hideCharJob by remember { mutableStateOf<Job?>(null) }

    var confirmLastCharVisible by remember { mutableStateOf(false) }
    var confirmHideCharJob by remember { mutableStateOf<Job?>(null) }

    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    LaunchedEffect(authState) {

        when (authState) {

            is AuthResultState.Success -> {
                Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                navController.navigate(AppScreens.MainNav.route) {
                    popUpTo(0) { inclusive = true }
                }
            }

            is AuthResultState.VerificationEmailSent -> {
                navController.navigate(AppScreens.Verification.route)
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
            onValueChange = {
                // Detect if new char is added
                val isAdding = it.length > password.length
                password = it

                if (isAdding) {
                    lastCharVisible = true
                    hideCharJob?.cancel()
                    hideCharJob = coroutineScope.launch {
                        delay(1000L)
                        lastCharVisible = false
                    }
                } else {
                    // If backspacing, hide last char immediately
                    lastCharVisible = false
                    hideCharJob?.cancel()
                }
            }
            ,
            label = { Text("Password") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = when {
                !isLoginMode || passwordVisible -> VisualTransformation.None
                else -> LastCharVisiblePasswordTransformation(lastCharVisible)
            }
            ,
            trailingIcon = {
                if(isLoginMode) {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if(!isLoginMode){
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    val isAdding = it.length > confirmPassword.length
                    confirmPassword = it

                    if (isAdding) {
                        confirmLastCharVisible = true
                        confirmHideCharJob?.cancel()
                        confirmHideCharJob = coroutineScope.launch {
                            delay(1000L)
                            confirmLastCharVisible = false
                        }
                    } else {
                        confirmLastCharVisible = false
                        confirmHideCharJob?.cancel()
                    }
                }
                ,
                label = { Text("Confirm Password") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (confirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    LastCharVisiblePasswordTransformation(confirmLastCharVisible)
                },
                trailingIcon = {
                    val icon = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = icon, contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password")
                    }
                },
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
                        Toast.makeText(context, "Please enter email and/or password", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        if (password == confirmPassword) {
                            viewModel.signupWithEmail(email, password)
//                            Toast.makeText(context, "Verification Email sent", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Please enter Email and/or Password!",
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

            is AuthResultState.Error -> {
                val error = (authState as AuthResultState.Error).error
                Text(error ?: "Unknown Error", color = MaterialTheme.colorScheme.error)
            }

            else -> {}
        }
    }
}
