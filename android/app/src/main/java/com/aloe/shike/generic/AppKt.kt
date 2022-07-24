package com.aloe.shike.generic

import android.util.Log
import com.aloe.shike.BuildConfig

private const val stackTraceLen = 4
fun String?.log(type: Int = Log.DEBUG, tag: String = "aloe", tr:Throwable?=null) {
  takeIf { BuildConfig.DEBUG }?.let {
    with(Thread.currentThread().stackTrace[stackTraceLen]) { "$methodName($fileName:$lineNumber) $it" }
  }?.also {
    when (type) {
      Log.DEBUG -> Log.d(tag, it, tr)
      Log.INFO -> Log.i(tag, it, tr)
      Log.WARN -> Log.w(tag, it, tr)
      Log.ERROR -> Log.e(tag, it, tr)
    }
  }
}
