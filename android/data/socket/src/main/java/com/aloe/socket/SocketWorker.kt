package com.aloe.socket

import android.app.Notification
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.JdkLoggerFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltWorker
internal class SocketWorker @AssistedInject constructor(@Assisted ctx: Context, @Assisted params: WorkerParameters) :
  CoroutineWorker(ctx, params) {
  @Inject
  lateinit var clientUdp: ClientUdp
  override suspend fun doWork(): Result {
    InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE)
    if (atomicCollect.compareAndSet(false, true)) {
      clientUdp.addUdpListener()
    }
    return Result.success()
  }

  override suspend fun getForegroundInfo(): ForegroundInfo {
    return ForegroundInfo(100, Notification())
  }

  companion object {
    private val atomicCollect: AtomicBoolean = AtomicBoolean(false)

    fun initSocket(ctx: Context) {
      WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
        "socket",
        ExistingPeriodicWorkPolicy.KEEP,
        PeriodicWorkRequestBuilder<SocketWorker>(1, TimeUnit.SECONDS).setConstraints(
          Constraints.Builder().setRequiresCharging(true).build()
        ).build()
      )
    }
  }
}
