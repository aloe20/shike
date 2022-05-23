package com.aloe.shike.app

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.aloe.shike.R

class AppService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onCreate() {
        super.onCreate()
        val channelId = "app_id_01"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, "f", NotificationManager.IMPORTANCE_MIN)
            notificationChannel.description = ""
            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            getSystemService<NotificationManager>()?.createNotificationChannel(notificationChannel)
        }
        val notification = NotificationCompat.Builder(this, channelId).setSmallIcon(R.drawable.icon_notification)
            .setShowWhen(false).setPriority(NotificationCompat.PRIORITY_MIN)
            .setCustomContentView(RemoteViews(packageName, R.layout.layout_notification)).build()
        startForeground(10, notification)
    }

    companion object {
        fun start(ctx: Context) {
            Intent(ctx, AppService::class.java).also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(it)
                else ctx.startService(it)
            }
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, AppService::class.java))
        }
    }
}
