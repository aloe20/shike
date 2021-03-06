package com.aloe.rn

import android.widget.Toast
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

class ReactToast(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
  override fun getName(): String = "RnToast"
  override fun getConstants(): MutableMap<String, Any> {
    return mutableMapOf("SHORT" to Toast.LENGTH_SHORT, "LONG" to Toast.LENGTH_LONG)
  }

  @ReactMethod
  fun show(msg: String, duration: Int) {
    Executors.newCachedThreadPool { r -> Thread(r, "a") }
    Toast.makeText(reactApplicationContext, msg, duration).show()
  }
}
