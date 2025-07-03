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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DiscoverScreen(
    uid: String,
    hasPermission: Boolean,
    isGpsOn: Boolean,
    onRequestPermission: () -> Unit,
    onEnableGps: () -> Unit,
    lastLocation: Location?,
    locationUpdateCount: Int
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            !hasPermission -> Button(onClick = onRequestPermission) { Text("Grant Location Permission") }
            !isGpsOn -> Button(onClick = onEnableGps) { Text("Enable Device Location") }
            else -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("UID: $uid", style = MaterialTheme.typography.bodyMedium)
                lastLocation?.let {
                    Text("Lat: ${it.latitude}")
                    Text("Lng: ${it.longitude}")
                } ?: Text("Waiting for location...")
                Text("Update #: $locationUpdateCount")
            }
        }
    }
}