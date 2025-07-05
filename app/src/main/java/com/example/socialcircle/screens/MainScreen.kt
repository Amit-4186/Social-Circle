package com.example.socialcircle.screens

import android.Manifest
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.socialcircle.ui.theme.Blue20
import com.example.socialcircle.viewModels.FriendsViewModel
import com.example.socialcircle.viewModels.DiscoverViewModel
import com.example.socialcircle.viewModels.LocationViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay

sealed class MainScreens(val route: String) {
    object Discover : MainScreens("discover")
    object ChatList : MainScreens("chatList")
    object Friend : MainScreens("friend")
    object Profile : MainScreens("profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(appNavController: NavController) {
    fun onChatClick(chatId: String) {
        appNavController.navigate("chat/$chatId") {
            launchSingleTop = true
            restoreState = true
        }
    }

    val uid = Firebase.auth.uid
    val mainNavController = rememberNavController()

    val context = LocalContext.current
    val discoverViewModel: DiscoverViewModel =
        viewModel(factory = LocationViewModelFactory(context))
    val friendsViewModel: FriendsViewModel = viewModel()

    var currentScreen by remember { mutableStateOf<MainScreens>(MainScreens.Discover) }

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
                    discoverViewModel.startLocationUpdates()
                }
                isGpsOn = enabled
                delay(1000)
            }
        }
    }

    val mainScreenMap = mapOf(
        MainScreens.Discover to "Nearby People",
        MainScreens.ChatList to "Chats",
        MainScreens.Friend to "Friends",
        MainScreens.Profile to "My Profile"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = mainScreenMap[currentScreen]!!, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue20,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = Blue20,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                shadowElevation = 8.dp
            ) {
                NavigationBar(
                    modifier = Modifier
                        .height(68.dp)
                        .padding(horizontal = 16.dp),
                    containerColor = Color.Transparent,
                ) {
                    mainScreenMap.forEach { (screen, _) ->
                        val selected = currentScreen == screen

                        val icon = when (screen.route) {
                            MainScreens.Discover.route -> Icons.Filled.People
                            MainScreens.ChatList.route -> Icons.Filled.ChatBubble
                            MainScreens.Friend.route -> Icons.Filled.Group
                            MainScreens.Profile.route -> Icons.Filled.Person
                            else -> Icons.Filled.Error
                        }

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = screen.route,
                                    tint = if (selected) Blue20 else Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            },
                            selected = selected,
                            onClick = {
                                currentScreen = screen
                                mainNavController.navigate(screen.route) {
                                    popUpTo(mainNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }

    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                startDestination = MainScreens.Discover.route,
                navController = mainNavController
            ) {
                composable(MainScreens.Discover.route) {
                    DiscoverScreen(
                        uid = uid!!,
                        hasPermission = hasPermission,
                        isGpsOn = isGpsOn,
                        onRequestPermission = { permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                        onEnableGps = {
                            discoverViewModel.checkLocationSettings { sender ->
                                settingsLauncher.launch(IntentSenderRequest.Builder(sender).build())
                            }
                        },
                        lastLocation = discoverViewModel.lastLocation.collectAsState().value,
                        discoverViewModel = discoverViewModel,
                        onChatClick = { chatId -> onChatClick(chatId) },
                        friendsViewModel = friendsViewModel,
                        onFriendRequest = { uid ->
                            friendsViewModel.sendFriendRequest(
                                uid,
                                { Toast.makeText(context, "Friend Request Sent", Toast.LENGTH_SHORT).show() },
                                { Toast.makeText(context, "Unexpected Error", Toast.LENGTH_SHORT).show() }
                            )
                        }
                    )
                }
                composable(MainScreens.ChatList.route) {
                    ChatListScreen(mainNavController) { chatId -> onChatClick(chatId) }
                }
                composable(MainScreens.Friend.route) {
                    FriendScreen(
                        friendsViewModel,
                        mainNavController
                    ) { chatId -> onChatClick(chatId) }
                }
                composable(MainScreens.Profile.route) {
                    ProfileScreen(mainNavController, appNavController)
                }
            }
        }
    }
}