package com.example.socialcircle

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.socialcircle.navigation.AppNavigation
import com.example.socialcircle.screens.SplashScreen
import com.example.socialcircle.ui.theme.SocialCircleTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        val currentVersionCode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
            } else {
                packageManager.getPackageInfo(packageName, 0).versionCode
            }
        setContent {
            SocialCircleTheme {
                var showApp by remember { mutableStateOf(false) }
                if (showApp) {
                    AppNavigation()
                } else {
                    SplashScreen(
                        currentVersionCode = currentVersionCode,
                        onReady = { showApp = true }
                    )
                }
            }
        }
    }
}