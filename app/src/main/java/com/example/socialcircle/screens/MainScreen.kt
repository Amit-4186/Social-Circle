package com.example.socialcircle.screens

import android.Manifest
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialcircle.Screen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay

@Composable
fun MainScreen() {
    val uid = Firebase.auth.uid

    val tabs = listOf(Screen.Chat, Screen.Discover, Screen.Profile)
    var currentRoute by remember { mutableStateOf(Screen.Discover.route) }
    val context = LocalContext.current
    val viewModel: LocationViewModel = viewModel(factory = LocationViewModelFactory(context))

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var isGpsOn by remember { mutableStateOf(false) }
    var updateTrigger by remember { mutableIntStateOf(0) }

    // Launchers
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        // system dialog returned, we rely on loop to detect
    }

    // Monitor permission and GPS state
    LaunchedEffect(hasPermission, updateTrigger) {
        if (hasPermission) {
            // Loop to check GPS status every second
            while (hasPermission) {
                val lm = context.getSystemService(LocationManager::class.java)
                val enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (enabled && !isGpsOn) {
                    // GPS just turned on
                    viewModel.startLocationUpdates()
                }
                isGpsOn = enabled
                delay(1000)
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            when (screen.route) {
                                "chat" -> Icon(Icons.Filled.ChatBubble, "Chat")
                                "discover" -> Icon(Icons.Filled.People, "Discover")
                                "profile" -> Icon(Icons.Filled.Person, "Profile")
                            }
                        },
                        label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                        selected = currentRoute == screen.route,
                        onClick = { currentRoute = screen.route }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentRoute) {

                Screen.Discover.route -> DiscoverScreen(
                    uid = uid!!,
                    hasPermission = hasPermission,
                    isGpsOn = isGpsOn,
                    onRequestPermission = { permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    onEnableGps = {
                        viewModel.checkLocationSettings { sender ->
                            settingsLauncher.launch(IntentSenderRequest.Builder(sender).build())
                        }
                    },
                    lastLocation = viewModel.lastLocation.collectAsState().value,
                    locationUpdateCount = viewModel.locationUpdateCount.collectAsState().value
                )

                Screen.Chat.route -> ChatScreen()

                Screen.Profile.route -> ProfileScreen()
            }
        }
    }
}