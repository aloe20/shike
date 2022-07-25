package com.aloe.shike.generic

import android.os.Handler
import android.os.HandlerThread

object AppThread : HandlerThread("app") {
  private val listRunnable = mutableListOf<Runnable>()
  private lateinit var handler: Handler
  override fun onLooperPrepared() {
    if (!this::handler.isInitialized) {
      handler = Handler(looper)
      while (listRunnable.isNotEmpty()) {
        handler.post(listRunnable.removeAt(0))
      }
    }
  }

  fun post(runnable: Runnable) {
    if (this::handler.isInitialized) {
      handler.post(runnable)
    }else{
      listRunnable.add(runnable)
    }
  }
}
