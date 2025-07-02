package com.example.socialcircle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.socialcircle.screens.ForgotPasswordScreen
import com.example.socialcircle.screens.LoadingScreen
import com.example.socialcircle.screens.LoginScreen
import com.example.socialcircle.screens.MainAppScreen
import com.example.socialcircle.screens.ProfileCreationScreen
import com.example.socialcircle.screens.ProfileSetupScreen
import com.example.socialcircle.screens.VerificationScreen
import com.example.socialcircle.viewModels.AuthenticationViewModel

sealed class Screen(val route: String) {
    object Loading : Screen("loading")
    object Login : Screen("login")
    object Verification : Screen("verification")
    object ForgotPassword : Screen("forgotPassword")
    object ProfileCreation : Screen("profileCreation")
    object Main : Screen("main")
    object Chat : Screen("chat")
    object Discover : Screen("discover")
    object Profile : Screen("profile")
    object ProfileSetup: Screen(route = "profileSetup")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: AuthenticationViewModel = viewModel()

    val isLoaded by remember { mutableStateOf(false) }
    val isLogin by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = Screen.Loading.route //Screen.ProfileSetup.route
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
            LoginScreen(viewModel, navController)
        }

        composable(Screen.Main.route) {
            MainAppScreen()
        }

        composable(Screen.Verification.route){
            VerificationScreen(viewModel, navController)
        }

        composable(Screen.ForgotPassword.route){
            ForgotPasswordScreen(viewModel, navController)
        }

        composable(Screen.ProfileCreation.route){
            ProfileCreationScreen(navController)
        }

        composable(Screen.ProfileSetup.route){
            ProfileSetupScreen()
        }
    }
}