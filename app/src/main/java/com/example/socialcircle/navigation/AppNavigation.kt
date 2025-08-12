package com.example.socialcircle.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.socialcircle.screens.ProfileSetupScreen
import com.example.socialcircle.screens.VerificationScreen
import com.example.socialcircle.viewModels.AuthenticationViewModel

sealed class AppScreens(val route: String) {
    object Login : AppScreens("login")
    object Verification : AppScreens("verification")
    object ForgotPassword : AppScreens("forgotPassword")
    object ProfileSetup : AppScreens(route = "profileSetup")
    object RootNav : AppScreens("rootNav")
}

@Composable
fun AppNavigation() {
    val appNav = rememberNavController()
    val authViewModel: AuthenticationViewModel = viewModel()

    val isLogin by remember { mutableStateOf(authViewModel.user != null) }
    val isVerified by remember { mutableStateOf(authViewModel.user?.isEmailVerified ?: false) }
    val userExists by authViewModel.uidExistsState.collectAsState()

    LaunchedEffect(Unit) {
        if (isLogin)
            authViewModel.checkProfileExists(authViewModel.user!!.uid)
        else {
            authViewModel.setUidExistsStateFalse()
        }
    }

    when (userExists) {
        null -> {
            LoadingScreen()
        }

        else -> {
            val initialRoute =
                if (userExists!!) AppScreens.RootNav.route else AppScreens.ProfileSetup.route
            NavHost(
                navController = appNav,
                startDestination = if (isLogin && isVerified) initialRoute else if (isLogin) AppScreens.Verification.route else AppScreens.Login.route
            ) {

                composable(AppScreens.Login.route) {
                    LoginScreen(authViewModel, appNav)
                }

                composable(AppScreens.Verification.route) {
                    VerificationScreen(authViewModel, appNav)
                }

                composable(AppScreens.ForgotPassword.route) {
                    ForgotPasswordScreen(authViewModel, appNav)
                }

                composable(AppScreens.ProfileSetup.route) {
                    ProfileSetupScreen(appNav)
                }

                composable(AppScreens.RootNav.route) {
                    RootNav(appNav)
                }
            }
        }
    }
}