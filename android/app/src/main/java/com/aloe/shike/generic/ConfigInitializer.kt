package com.aloe.shike.generic

import android.content.Context
import androidx.startup.Initializer
import com.aloe.rn.ReactView

@Suppress("unused")
class ConfigInitializer:Initializer<Unit> {
  override fun create(context: Context) {
    (context as App).repository = Repository(context)
    AppThread.start()
    AppThread.post { ReactView.initRn(context) }
  }

  override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
