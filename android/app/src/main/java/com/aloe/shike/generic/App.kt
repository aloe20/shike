package com.aloe.shike.generic

import android.app.Application
import com.aloe.rn.ReactView
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    AppThread.start()
    AppThread.post { ReactView.initRn(this) }
  }
}
