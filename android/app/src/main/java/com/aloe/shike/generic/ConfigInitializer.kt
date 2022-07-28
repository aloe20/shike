package com.aloe.shike.generic

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.startup.Initializer

@Suppress("unused")
class ConfigInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    ProcessLifecycleOwner.get().lifecycle.addObserver(context as App)
  }

  override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
