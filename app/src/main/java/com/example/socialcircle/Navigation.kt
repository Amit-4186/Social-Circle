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
import com.example.socialcircle.screens.ProfileSetupScreen
import com.example.socialcircle.screens.VerificationScreen
import com.example.socialcircle.viewModels.AuthenticationViewModel

sealed class AppScreens(val route: String) {
    object Login : AppScreens("login")
    object Verification : AppScreens("verification")
    object ForgotPassword : AppScreens("forgotPassword")
    object ProfileSetup: AppScreens(route = "profileSetup")
    object MainNav : AppScreens("mainNav")
}

@Composable
fun AppNavigation() {
    val appNavController = rememberNavController()
    val authViewModel: AuthenticationViewModel = viewModel()

    val isLogin by remember { mutableStateOf(authViewModel.user != null) }

    NavHost(
        navController = appNavController,
        startDestination = if (isLogin) AppScreens.MainNav.route else AppScreens.Login.route
    ) {

        composable(AppScreens.Login.route) {
            LoginScreen(authViewModel, appNavController)
        }

        composable(AppScreens.Verification.route){
            VerificationScreen(authViewModel, appNavController)
        }

        composable(AppScreens.ForgotPassword.route){
            ForgotPasswordScreen(authViewModel, appNavController)
        }

        composable(AppScreens.ProfileSetup.route){
            ProfileSetupScreen(appNavController)
        }

        composable(AppScreens.MainNav.route) {
            MainScreen(appNavController)
        }

//        composable(
//            route = AppScreens.Chat.route,
//            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
//        ) { backStackEntry ->
//            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
//            ChatScreen(chatId)
//        }
    }
}