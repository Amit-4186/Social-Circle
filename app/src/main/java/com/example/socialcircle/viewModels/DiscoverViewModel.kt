package com.example.socialcircle.viewModels

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.socialcircle.models.ProfileDetails
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
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class DiscoverViewModel(
    private val fusedClient: FusedLocationProviderClient,
    private val settingsClient: SettingsClient
) : ViewModel() {
    val uid = Firebase.auth.uid
    private val db = Firebase.firestore

    private val firestore = Firebase.firestore
    private val locationCollection = firestore.collection("UserLocation")

    private val _lastLocation = MutableStateFlow<Location?>(null)
    val lastLocation: StateFlow<Location?> = _lastLocation

    private val _nearbyProfiles = MutableStateFlow<List<ProfileDetails>>(emptyList())
    val nearbyProfiles = _nearbyProfiles

    private var locationUpdatesStarted = false

    fun publishLocation(uid: String, location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        val userLocationData = mapOf(
            "uid" to uid,
            "geopoint" to geoPoint,
            "timestamp" to FieldValue.serverTimestamp()
        )
        locationCollection.document(uid).set(userLocationData)
    }

    // Schedule periodic refresh every 5 minutes
    init {
        viewModelScope.launch {
            // initial delay to wait for first location
//            delay(5_000)
            while (isActive) {
                lastLocation.value?.let { loc ->
                    refreshNearby(loc)
                }
                delay(60 * 1000L) // updated after 10 minutes
            }
        }
    }

    fun fetchNearbyProfiles(
        userIds: List<String>
    ) {
        if (userIds.isEmpty()) {
            _nearbyProfiles.value = emptyList()
            return
        }

        val chunks = userIds.chunked(10)
        val totalChunks = chunks.size
        val tempList = mutableListOf<ProfileDetails>()
        var completedChunks = 0

        for (chunk in chunks) {
            db.collection("UserProfiles")
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .addOnSuccessListener { result ->
                    val users = result.documents.mapNotNull { doc ->
                        doc.toObject(ProfileDetails::class.java)
                    }
                    tempList.addAll(users)
                    Log.d("mine", "successful ${result.size()}")
                    completedChunks++

                    if (completedChunks == totalChunks) {
                        _nearbyProfiles.value = tempList
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("mine", "failed: ${e.message}")
                    completedChunks++
                    if (completedChunks == totalChunks) {
                        _nearbyProfiles.value = tempList
                    }
                }
        }
    }

    fun refreshNearby(currentLoc: Location) {
        viewModelScope.launch{
            val allDocs = firestore.collection("UserLocation").get().await()
            val nearby = allDocs.documents.mapNotNull { doc ->
                val geo = doc.getGeoPoint("geopoint") ?: return@mapNotNull null
                val dist = distanceInKm(
                    currentLoc.latitude, currentLoc.longitude,
                    geo.latitude, geo.longitude
                )
                if (dist <= 2.0) doc.id else null
            }
            fetchNearbyProfiles(nearby)
//            _nearbyUserIds.value = nearby
//            _refreshTrigger.update { it + 1 }
        }
    }

    fun distanceInKm(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val radius = 6371.0 // Earth radius
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat/2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng/2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1-a))
        return radius * c
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let {
                publishLocation(uid!!, it) // pushing location on firestore
                Log.d("geoPoint Publish", "Lat: ${it.latitude} Lng: ${it.longitude}")
            }
            _lastLocation.value = result.lastLocation //temporary
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
        return DiscoverViewModel(fusedClient, settingsClient) as T
    }
}
