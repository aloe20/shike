package com.aloe.socket

internal class SocketWorker /*constructor(ctx: Context, params: WorkerParameters) :
  CoroutineWorker(ctx, params)*/ {
  /*private lateinit var clientUdp: ClientUdp
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
  }*/
}
