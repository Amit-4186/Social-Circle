package com.example.socialcircle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.socialcircle.screens.LoadingScreen
import com.example.socialcircle.screens.LoginScreen
import com.example.socialcircle.screens.MainAppScreen

sealed class Screen(val route: String) {
    object Loading : Screen("loading")
    object Login : Screen("login")
    object Main : Screen("main")
    object Chat : Screen("chat")
    object Discover : Screen("discover")
    object Profile : Screen("profile")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val isLoaded by remember { mutableStateOf(false) }
    val isLogin by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = Screen.Loading.route
    ) {

        fun loadingComplete() {
            if (isLogin) {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Loading.route) { inclusive = true }
                }
            }
            else {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Loading.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Loading.route) {
            LoadingScreen(isLoaded = isLoaded, loadingComplete = { loadingComplete() })
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainAppScreen()
        }
    }
}