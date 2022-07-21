package com.aloe.shike.simple

import android.app.Application
import com.facebook.soloader.SoLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(){
  override fun onCreate() {
    super.onCreate()
    SoLoader.init(this, false)
  }
}
