package com.aloe.shike.app

import android.content.Context
import androidx.startup.Initializer

@Suppress("unused")
class AppInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        /*StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().detectCustomSlowCalls().detectDiskReads()
                .detectDiskWrites().detectNetwork().penaltyLog().build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build()
        )*/
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
