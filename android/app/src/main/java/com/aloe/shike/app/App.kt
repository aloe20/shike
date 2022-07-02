package com.aloe.shike.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.Executors
import javax.inject.Inject
import okhttp3.OkHttpClient

@HiltAndroidApp
class App : Application(), Configuration.Provider, ImageLoaderFactory {
  @Inject
  lateinit var factory: HiltWorkerFactory

  @Inject
  lateinit var okHttpClient: OkHttpClient
  lateinit var appThread: AppThread

  @SuppressLint("ShowToast")
  override fun onCreate() {
    super.onCreate()
    // Thread.dumpStack()
    // UI线程空闲时创建全局toast对象
    //Looper.myQueue().addIdleHandler { false.also {  } }
    appThread = AppThread(this)
    appThread.start()
  }

  override fun getWorkManagerConfiguration() =
    Configuration.Builder().setExecutor(Executors.newFixedThreadPool(8)).setMinimumLoggingLevel(Log.DEBUG).setWorkerFactory(factory).build()

  override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this).crossfade(true)
    .okHttpClient(okHttpClient).build()
}

fun Context.showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
  (applicationContext as? App)?.appThread?.showToast(text, duration)
}
