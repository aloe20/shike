package com.aloe.shike.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Looper
import android.widget.Toast
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import okhttp3.OkHttpClient

@HiltAndroidApp
class App : Application(), Configuration.Provider, DefaultLifecycleObserver, ImageLoaderFactory {
    @Inject
    lateinit var factory: HiltWorkerFactory

    @Inject
    lateinit var okHttpClient: OkHttpClient
    lateinit var toast: Toast

    @SuppressLint("ShowToast")
    override fun onCreate() {
        super<Application>.onCreate()
        // Thread.dumpStack()
        // UI线程空闲时创建全局toast对象
        Looper.myQueue().addIdleHandler { false.also { toast = Toast.makeText(this, "", Toast.LENGTH_SHORT) } }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun getWorkManagerConfiguration() = Configuration.Builder().setWorkerFactory(factory).build()

    override fun onStart(owner: LifecycleOwner) {
        AppService.stop(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        AppService.start(this)
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this).crossfade(true)
        .okHttpClient(okHttpClient).build()
}

fun Context.showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    (applicationContext as? App)?.toast?.also {
        it.setText(text)
        it.duration = duration
    }?.show()
}
