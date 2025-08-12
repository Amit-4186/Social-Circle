package com.example.socialcircle.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.socialcircle.screens.ChatListScreen
import com.example.socialcircle.screens.ChatScreen
import com.example.socialcircle.screens.DiscoverScreen
import com.example.socialcircle.screens.FriendScreen
import com.example.socialcircle.screens.ProfileEditScreen
import com.example.socialcircle.screens.ProfileScreen
import com.example.socialcircle.ui.theme.Blue20
import com.example.socialcircle.viewModels.ChatViewModel
import com.example.socialcircle.viewModels.DiscoverViewModel
import com.example.socialcircle.viewModels.FriendsViewModel
import com.example.socialcircle.viewModels.ProfileViewModel
import kotlinx.coroutines.delay

sealed class MainScreens(val route: String) {
    object Discover : MainScreens("discover")
    object ChatList : MainScreens("chatList")
    object Friend : MainScreens("friend")
    object Profile : MainScreens("profile")
    object ProfileEdit : MainScreens("profileEdit")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    appNav: NavController, rootNav: NavController,
    discoverViewModel: DiscoverViewModel,
    chatViewModel: ChatViewModel,
    friendsViewModel: FriendsViewModel,
    profileViewModel: ProfileViewModel
) {

    val mainNavController = rememberNavController()
    val currentBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    fun onChatClick(chatId: String) {
        rootNav.navigate(RootScreens.Chat.createRoute(chatId)) {
            launchSingleTop = true
            restoreState = true
        }
    }

    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
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
        MainScreens.Discover.route to "Nearby People",
        MainScreens.ChatList.route to "Chats",
        MainScreens.Friend.route to "Friends",
        MainScreens.Profile.route to "My Profile"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = mainScreenMap[currentDestination?.route] ?: "",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue20,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Blue20,
                tonalElevation = 4.dp,

                modifier = Modifier.fillMaxWidth()
            ) {
                mainScreenMap.forEach { (screen, _) ->
                    val selected = currentDestination?.route == screen

                    val icon = when (screen) {
                        MainScreens.Discover.route -> if (selected) Icons.Filled.PersonSearch else Icons.Outlined.PersonSearch
                        MainScreens.ChatList.route -> if (selected) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubble
                        MainScreens.Friend.route -> if (selected) Icons.Filled.Group else Icons.Outlined.Group
                        MainScreens.Profile.route -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
                        else -> Icons.Filled.Error
                    }

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            mainNavController.navigate(screen) {
                                popUpTo(mainNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = screen,
                                modifier = Modifier.size(26.dp),
                                tint = if (selected) Color.White else Color.White.copy(alpha = 0.6f)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Blue20
                        )
                    )
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
                        friendsViewModel = friendsViewModel
                    )
                }
                composable(MainScreens.ChatList.route) {
                    ChatListScreen(
                        chatViewModel
                    ) { chatId -> onChatClick(chatId) }
                }
                composable(MainScreens.Friend.route) {
                    FriendScreen(
                        friendsViewModel,
                        mainNavController
                    ) { chatId -> onChatClick(chatId) }
                }
                composable(MainScreens.Profile.route) {
                    ProfileScreen(mainNavController, appNav, profileViewModel)
                }
                composable(MainScreens.ProfileEdit.route) {
                    ProfileEditScreen(mainNavController, profileViewModel)
                }
                composable(
                    route = "chat/{chatId}",
                    arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable

                    ChatScreen(chatViewModel, chatId)
                }
            }
        }
    }
}