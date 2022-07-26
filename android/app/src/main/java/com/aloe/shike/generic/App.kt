package com.aloe.shike.generic

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class App : Application(), DefaultLifecycleObserver {
  lateinit var repository: Repository

  override fun onStart(owner: LifecycleOwner) {
    "app on start".log()
  }

  override fun onStop(owner: LifecycleOwner) {
    "app on stop".log()
  }
}
