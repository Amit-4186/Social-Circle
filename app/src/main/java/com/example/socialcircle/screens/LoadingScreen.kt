package com.example.socialcircle.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun LoadingScreen(isLoaded: Boolean, loadingComplete: () -> Unit) {
    if(isLoaded){
        loadingComplete
    }
    else {
        Column {
            CircularProgressIndicator()
            Button(onClick = loadingComplete) {
                Text("Load")
            }
        }
    }
}