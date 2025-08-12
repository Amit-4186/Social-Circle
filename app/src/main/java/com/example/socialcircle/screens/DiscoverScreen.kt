package com.example.socialcircle.screens

import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WrongLocation
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.socialcircle.R
import com.example.socialcircle.ui.theme.Blue20
import com.example.socialcircle.viewModels.DiscoverViewModel
import com.example.socialcircle.viewModels.FriendsViewModel
import kotlinx.coroutines.delay

@Composable
fun DiscoverScreen(
    friendsViewModel: FriendsViewModel,
    hasPermission: Boolean,
    isGpsOn: Boolean,
    onRequestPermission: () -> Unit,
    onEnableGps: () -> Unit,
    lastLocation: Location?,
    discoverViewModel: DiscoverViewModel,
    onChatClick: (String) -> Unit
) {
//    lastLocation?.let {
//        Log.d("geopoint", "Lat: ${it.latitude} Lng: ${it.longitude}")
//    } ?: Log.d("geopoint", "Waiting for location...")
    LaunchedEffect(isGpsOn, hasPermission) {
        lastLocation?.let { discoverViewModel.refreshNearby(it) }
    }
    lastLocation?.let { discoverViewModel.refreshNearby(it) }

    val context = LocalContext.current
    val nearbyProfiles by discoverViewModel.nearbyProfiles.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF66B5FF),
                        Color(0xFFCCE6FF),
                        Color.White
                    ),
                    radius = 1000f
                )
            ), contentAlignment = Alignment.Center
    ) {
        when {
            !hasPermission ->
                LocationPermission(onRequestPermission)

            !isGpsOn ->
                LocationEnable(onEnableGps)

            else ->
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    LottieInfiniteAnimation(
                        assetName = "discover_animation.json",
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(.8f)
                    ) {
                        when {
                            nearbyProfiles.isEmpty() -> Text("No People Nearby..")
                            else -> nearbyProfiles.forEach { profile ->
                                if (profile.uid != discoverViewModel.uid) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .height(48.dp)
                                            .background(
                                                color = Color.White.copy(alpha = .4f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .weight(.7f)
                                                .fillMaxHeight()
                                        ) {
                                            AsyncImage(
                                                model = profile.photoUrl,
                                                placeholder = painterResource(R.drawable.profile_loading),
                                                contentDescription = "Profile Picture",
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(100))
                                            )
                                            Text(
                                                profile.name,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onChatClick(profile.uid) },
                                            modifier = Modifier
                                                .weight(.15f)
                                                .height(18.dp)
                                        ) {
                                            Icon(Icons.Outlined.ChatBubbleOutline, "Send Chat")
                                        }
                                        IconButton(
                                            onClick = {
                                                friendsViewModel.sendFriendRequest(
                                                    profile.uid,
                                                    {
                                                        Toast.makeText(
                                                            context,
                                                            "Friend Request Sent",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    },
                                                    {
                                                        Toast.makeText(
                                                            context,
                                                            "Unexpected Error",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                )
                                            },
                                            modifier = Modifier
                                                .weight(.15f)
                                                .height(18.dp)
                                        ) {
                                            Icon(Icons.Outlined.PersonAdd, "Add Friend")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    var isButtonEnabled by remember { mutableStateOf(true) }

                    IconButton(
                        onClick = {
                            if (isButtonEnabled) {
                                lastLocation?.let { discoverViewModel.refreshNearby(it) }
                                isButtonEnabled = false
                            }
                        },
                        modifier = Modifier
                            .padding(12.dp)
                            .shadow(4.dp, shape = CircleShape)
                            .background(color = Blue20)
                            .align(Alignment.BottomCenter)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }

                    if (!isButtonEnabled) {
                        LaunchedEffect(Unit) {
                            delay(5000)
                            isButtonEnabled = true
                        }
                    }
                }
        }
    }
}

@Composable
fun LocationPermission(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                tint = Blue20,
                contentDescription = "Location Permission",
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = "Location Permission",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "In order to find people nearby and socialise with them, allow location permission.",
                textAlign = TextAlign.Center
            )
            Button(onClick = onClick, shape = RectangleShape) {
                Text("Request Permission", color = Color.White)
            }
        }
    }
}

@Composable
fun LocationEnable(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.WrongLocation,
            tint = Blue20,
            contentDescription = "Enable Location",
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = "Enable Location",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "In order to find people nearby and socialise with them, enable your device location.",
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onClick, shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Blue20)
            ) {
            Text("Enable GPS", color = Color.White)
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
