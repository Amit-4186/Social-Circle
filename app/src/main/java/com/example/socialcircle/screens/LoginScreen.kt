package com.example.socialcircle.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    Button(onClick = onLoginSuccess) {
        Text("Login")
    }
}