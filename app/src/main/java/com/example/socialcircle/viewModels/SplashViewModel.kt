package com.example.socialcircle.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

sealed class SplashState {
    object Loading : SplashState()
    object NoInternet : SplashState()
    object UpToDate : SplashState()
    data class Outdated(val githubUrl: String) : SplashState()
    object Error : SplashState()
}

data class RemoteConfig(val latestVersionCode: Int, val githubUrl: String)

class SplashViewModel(private val currentVersionCode: Int) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state

    init {
        checkAll()
    }

    fun retry() = checkAll()

    private fun checkAll() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!NetworkUtils.isInternetAvailable()) {
                _state.value = SplashState.NoInternet
                return@launch
            }
            try {
                val config = fetchRemoteConfig()
                Log.d("remoteConfig","${config.latestVersionCode}  $currentVersionCode")
                if (currentVersionCode >= config.latestVersionCode) {
                    _state.value = SplashState.UpToDate
                } else {
                    _state.value = SplashState.Outdated(config.githubUrl)
                }
            } catch (_: Exception) {
                _state.value = SplashState.Error
            }
        }
    }

    private fun fetchRemoteConfig(): RemoteConfig {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://raw.githubusercontent.com/Amit-4186/The-Social-Circle-App/refs/heads/main/version.json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed to fetch config")
            val body = response.body.string()
            val json = JSONObject(body)
            return RemoteConfig(
                latestVersionCode = json.getInt("latestVersionCode"),
                githubUrl = json.getString("githubUrl")
            )
        }
    }
}

object NetworkUtils {
    fun isInternetAvailable(): Boolean {
        return try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://clients3.google.com/generate_204")
                .build()
            client.newCall(request).execute().use {
                it.isSuccessful
            }
        } catch (e: Exception) {
            Log.d("mine", e.message.toString())
            false
        }
    }
}