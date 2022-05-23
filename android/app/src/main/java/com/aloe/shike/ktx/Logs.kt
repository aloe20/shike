package com.aloe.shike.ktx

import android.util.Log
import com.aloe.shike.BuildConfig

private const val stackTraceLen = 4
fun String?.log(type: Int = Log.DEBUG, tag: String = "aloe") {
    takeIf { BuildConfig.DEBUG }?.let {
        with(Thread.currentThread().stackTrace[stackTraceLen]) { "$methodName($fileName:$lineNumber) $it" }
    }?.also {
        when (type) {
            Log.DEBUG -> Log.d(tag, it)
            Log.INFO -> Log.i(tag, it)
            Log.WARN -> Log.w(tag, it)
            Log.ERROR -> Log.e(tag, it)
        }
    }
}
