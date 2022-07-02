package com.aloe.shike.app

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.provider.MediaStore
import android.widget.Toast

class AppThread(private val app: App) : HandlerThread("app_thread") {
  private val toast = Toast.makeText(app, "", Toast.LENGTH_SHORT)
  private val handler: Handler by lazy {
    object : Handler(looper) {
      override fun handleMessage(msg: Message) {
        if (msg.what == 0x10) {
          toast.show()
        }
      }
    }
  }

  override fun onLooperPrepared() {
    app.contentResolver.registerContentObserver(
      MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
      true,
      AppObserver(app, handler)
    )
  }

  fun showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    toast.setText(text)
    toast.duration = duration
    if (Looper.myLooper() == null) {
      handler.sendEmptyMessage(0x10)
    } else {
      toast.show()
    }
  }
}
