package com.aloe.shike.generic

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import okhttp3.OkHttpClient

@HiltAndroidApp
class App : Application(), DefaultLifecycleObserver, ImageLoaderFactory {
  @Inject
  lateinit var repository: Repository

  @Inject
  lateinit var httpClient: OkHttpClient

  override fun onStart(owner: LifecycleOwner) {
    "app on start".log()
  }

  override fun onStop(owner: LifecycleOwner) {
    "app on stop".log()
  }

  override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this).okHttpClient(httpClient)
    .crossfade(true).memoryCache { MemoryCache.Builder(this).maxSizePercent(0.25).build() }
    .diskCache { DiskCache.Builder().directory(cacheDir.resolve("image_cache")).maxSizePercent(0.02).build() }
    .build()
}
