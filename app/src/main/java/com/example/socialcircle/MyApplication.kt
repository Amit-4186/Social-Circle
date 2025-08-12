package com.example.socialcircle

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

@HiltAndroidApp
class MyApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        val imageCacheDir = File(cacheDir, "coil_image_cache")

        val diskCache = DiskCache.Builder()
            .directory(imageCacheDir)
            .maxSizeBytes(200L * 1024 * 1024) // 200 MB
            .build()

        val httpCacheDir = File(cacheDir, "http_cache")
        val okHttpClient = OkHttpClient.Builder()
            .cache(Cache(httpCacheDir, 50L * 1024 * 1024)) // 50 MB
            .addNetworkInterceptor { chain ->
                val res = chain.proceed(chain.request())
                res.newBuilder()
                    .header("Cache-Control", "public, max-age=${60 * 60 * 24 * 15}") // 15 days
                    .build()
            }
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .diskCache { diskCache }
            .respectCacheHeaders(false)
            .logger(DebugLogger())
            .build()
    }
}
