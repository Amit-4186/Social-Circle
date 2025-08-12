package com.example.socialcircle.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.socialcircle.screens.ChatScreen
import com.example.socialcircle.viewModels.ChatViewModel
import com.example.socialcircle.viewModels.DiscoverViewModel
import com.example.socialcircle.viewModels.FriendsViewModel
import com.example.socialcircle.viewModels.LocationViewModelFactory
import com.example.socialcircle.viewModels.ProfileViewModel

sealed class RootScreens(val route: String) {
    object MainNav : RootScreens("mainNav")
    object Chat: RootScreens("chat/{chatId}"){
        fun createRoute(chatId: String): String = "chat/$chatId"
    }
}

@Composable
fun RootNav(appNav: NavController){
    val rootNav = rememberNavController()

    val context = LocalContext.current
    val discoverViewModel: DiscoverViewModel =
        viewModel(factory = LocationViewModelFactory(context))
    val friendsViewModel: FriendsViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    NavHost(navController = rootNav, startDestination = RootScreens.MainNav.route){
        composable(RootScreens.MainNav.route) {
            MainScreen(
                appNav = appNav, rootNav = rootNav,
                discoverViewModel, chatViewModel,
                friendsViewModel, profileViewModel
            )
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