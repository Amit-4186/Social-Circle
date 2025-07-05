package com.example.socialcircle.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.socialcircle.AppScreens
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(mainNavController: NavController, appNavController: NavController) {
    Button(onClick = {
        FirebaseAuth.getInstance().signOut()
        appNavController.navigate(AppScreens.Login.route){
            popUpTo(0)
            launchSingleTop = true
        }
    }) {
        Text("Log Out")
    }
}