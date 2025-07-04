package com.example.socialcircle.screens

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun DiscoverScreen(
    uid: String,
    hasPermission: Boolean,
    isGpsOn: Boolean,
    onRequestPermission: () -> Unit,
    onEnableGps: () -> Unit,
    lastLocation: Location?,
    locationUpdateCount: Int,
    viewModel: LocationViewModel
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val nearby by viewModel.nearbyUsers.collectAsState()
        when {
            !hasPermission ->
                Button(onClick = onRequestPermission) { Text("Grant Location Permission") }

            !isGpsOn ->
                Button(onClick = onEnableGps) { Text("Enable Device Location") }

            else ->
                Box(contentAlignment = Alignment.Center) {
                    LottieInfiniteAnimation(
                        assetName = "discover.json",
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("UID: $uid", style = MaterialTheme.typography.bodyMedium)
                        lastLocation?.let {
                            Text("Lat: ${it.latitude} Lng: ${it.longitude}")
                        } ?: Text("Waiting for location...")

                        Text("Update #: $locationUpdateCount")

                        Button(onClick = {
                            lastLocation?.let { viewModel.refreshNearby(it) }
                        }) {
                            Text("Refresh Nearby Users")
                        }

                        Text("Users Nearby (2km Radius) :")

                        nearby.forEach { uid ->
                            Text(text = uid)
                        }
                    }
                }
        }
    }
}

@Composable
fun LottieInfiniteAnimation(
    assetName: String,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(assetName))

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true,
        restartOnPlay = false
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}
