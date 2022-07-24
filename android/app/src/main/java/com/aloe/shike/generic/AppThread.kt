package com.aloe.shike.generic

import android.os.Handler
import android.os.HandlerThread

object AppThread:HandlerThread("app") {
  private lateinit var handler:Handler
  override fun onLooperPrepared() {
    if (!this::handler.isInitialized) {
      handler = Handler(looper)
    }
  }

  fun post(runnable: Runnable){
    handler.post(runnable)
  }
}
