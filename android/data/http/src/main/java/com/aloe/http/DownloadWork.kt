package com.aloe.http

import android.content.Context
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.work.*
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import okio.source
import retrofit2.HttpException

internal class DownloadWork constructor(
  private val ctx: Context,
  private val params: WorkerParameters
) : CoroutineWorker(ctx, params) {

  lateinit var httpApi: HttpApi
  override suspend fun doWork(): Result {
    var filePath = ""
    var code = 0
    val result = params.inputData.getString("url")?.let { url ->
      withContext(Dispatchers.IO) {
        runCatching {
          val name = url.substring(url.lastIndexOf("/") + 1)
          val file = File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name)
          filePath = file.path
          val sink1 = file.sink(true)
          val sink = sink1.buffer()
          val body = httpApi.download(url, "bytes=${file.length()}-")
          val length = body.contentLength()
          var size = file.length()
          if (size < length) {
            val buffer = body.byteStream().source().buffer()
            val bytes = ByteArray(SIZE)
            while (size < length) {
              buffer.read(bytes).takeIf { it > -1 }?.also {
                size += it
                sink.write(bytes, 0, it)
                val progress = (1F * size * MAX_PROGRESS / length).toInt()
                setProgress(workDataOf("progress" to progress))
              }
            }
            sink.flush()
            sink.close()
            sink1.close()
          } else {
            setProgress(workDataOf("progress" to MAX_PROGRESS))
          }
        }.onFailure {
          code = (it as? HttpException)?.code() ?: 0
          println("--> $it")
        }
      }
    }
    return if (result?.isSuccess == true || code == CODE) Result.success(workDataOf("file" to filePath))
    else Result.failure()
  }

  companion object {
    private const val CODE = 416
    private const val MAX_PROGRESS = 100
    private const val SIZE = 8192
    fun download(context: Context, url: String): LiveData<WorkInfo> {
      val tag = "download"
      val workManager = WorkManager.getInstance(context)
      workManager.cancelAllWorkByTag(tag)
      val worker = OneTimeWorkRequestBuilder<DownloadWork>().setConstraints(Constraints.Builder().build())
        .addTag(tag).setInputData(Data.Builder().putString("url", url).build()).build()
      workManager.enqueue(worker)
      return workManager.getWorkInfoByIdLiveData(worker.id)
    }
  }
}
