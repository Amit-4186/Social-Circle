package com.example.socialcircle.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationViewModel(
    private val fusedClient: FusedLocationProviderClient,
    private val settingsClient: SettingsClient
) : ViewModel() {
    val uid = Firebase.auth.uid
    private val firestore = Firebase.firestore
    private val locationCollection = firestore.collection("UserLocation")

    private val _lastLocation = MutableStateFlow<Location?>(null)
    val lastLocation: StateFlow<Location?> = _lastLocation

    private val _locationUpdateCount = MutableStateFlow(0)
    val locationUpdateCount: StateFlow<Int> = _locationUpdateCount

    private var locationUpdatesStarted = false

    fun publishLocation(uid: String, location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        val userLocationData = mapOf(
            "uid" to uid,
            "geopoint" to geoPoint,
            "timestamp" to FieldValue.serverTimestamp()
        )

        locationCollection.document(uid).set(userLocationData)
        Log.d("location", "location published")
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let {
                publishLocation(uid!!, it) // pushing location on firestore
            }
            _lastLocation.value = result.lastLocation //temporary
            _locationUpdateCount.value += 1 //temporary
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (locationUpdatesStarted) return
        locationUpdatesStarted = true

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, // Will set it to balanced power accuracy
            15 * 1000L // 5 minutes set
        )
            .setMinUpdateDistanceMeters(0f) // minimum distance for callback
            .build()

        fusedClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        if (!locationUpdatesStarted) return
        fusedClient.removeLocationUpdates(locationCallback)
        locationUpdatesStarted = false
    }

    fun checkLocationSettings(
        onResolutionRequired: (IntentSender) -> Unit
    ) {
        val request = LocationSettingsRequest.Builder()
            .addLocationRequest(
                LocationRequest.Builder(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    10 * 1000L
                )
                    .setMinUpdateDistanceMeters(0f)
                    .build()
            )
            .setAlwaysShow(true)
            .build()

        settingsClient.checkLocationSettings(request)
            .addOnSuccessListener {
                // Location settings are satisfied
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    onResolutionRequired(e.resolution.intentSender)
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}

class LocationViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val settingsClient = LocationServices.getSettingsClient(context)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocationViewModel(fusedClient, settingsClient) as T
    }
}
