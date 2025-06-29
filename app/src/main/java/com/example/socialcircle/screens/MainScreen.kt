package com.example.socialcircle.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.socialcircle.Screen

@Composable
fun MainAppScreen() {
    val tabs = listOf(Screen.Chat, Screen.Discover, Screen.Profile)
    var currentRoute by remember { mutableStateOf(Screen.Chat.route) }

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
                        onClick = {
                            currentRoute = screen.route
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentRoute) {
                Screen.Chat.route -> ChatScreen()
                Screen.Discover.route -> DiscoverScreen()
                Screen.Profile.route -> ProfileScreen()
            }
        }
    }
}