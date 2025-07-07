package com.example.socialcircle.screens

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialcircle.viewModels.SplashState
import com.example.socialcircle.viewModels.SplashViewModel

@Composable
fun SplashScreen(
    currentVersionCode: Int,
    onReady: () -> Unit
) {

    val factory = remember(currentVersionCode) {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return SplashViewModel(currentVersionCode) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    val splashViewModel: SplashViewModel = viewModel(factory = factory)

    val state by splashViewModel.state.collectAsState()

    when (state) {
        SplashState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        SplashState.NoInternet -> NoInternetScreen { splashViewModel.retry() }

        SplashState.UpToDate -> LaunchedEffect(Unit) { onReady() }

        is SplashState.Outdated -> UpdateDialog((state as SplashState.Outdated).githubUrl)

        SplashState.Error -> ErrorScreen { splashViewModel.retry() }
    }
}

@Composable
fun NoInternetScreen(onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No Internet Connection")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun ErrorScreen(onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Unexpected Error!")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun UpdateDialog(githubUrl: String) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {},
        title = { Text("Update Available") },
        text = { Text("Please update to the latest version.") },
        confirmButton = {
            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, githubUrl.toUri())
                context.startActivity(intent)
            }) {
                Text("Update")
            }
        }
    )
}
