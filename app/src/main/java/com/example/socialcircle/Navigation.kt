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
import com.example.socialcircle.screens.LoginScreen
import com.example.socialcircle.screens.MainScreen
import com.example.socialcircle.screens.ProfileCreationScreen
import com.example.socialcircle.screens.ProfileSetupScreen
import com.example.socialcircle.screens.VerificationScreen
import com.example.socialcircle.viewModels.AuthenticationViewModel

sealed class Screen(val route: String) {
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

    val isLogin by remember { mutableStateOf(viewModel.user != null) }

    NavHost(
        navController = navController,
        startDestination = if (isLogin) Screen.Main.route else Screen.Login.route
    ) {

        composable(Screen.Login.route) {
            LoginScreen(viewModel, navController)
        }

        composable(Screen.Main.route) {
            MainScreen()
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