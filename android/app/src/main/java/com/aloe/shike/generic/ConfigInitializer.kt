package com.aloe.shike.generic

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.startup.Initializer
import com.aloe.rn.ReactView

@Suppress("unused")
class ConfigInitializer:Initializer<Unit> {
  override fun create(context: Context) {
    AppThread.start()
    AppThread.post { ReactView.initRn(context) }
    (context as App).also {
      it.repository = Repository(it)
      ProcessLifecycleOwner.get().lifecycle.addObserver(it)
    }
  }

  override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
