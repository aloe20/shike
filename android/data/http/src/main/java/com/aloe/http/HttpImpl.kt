package com.aloe.http

import android.content.Context
import android.os.Environment
import com.aloe.bean.ArticleBean
import com.aloe.bean.BannerBean
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import okio.source

internal class HttpImpl constructor(private val ctx: Context, private val api: HttpApi) : IHttp {
  override suspend fun loadBanner(): Result<List<BannerBean>?> = runCatching { api.loadBanner().data }
  override suspend fun loadTop(): Result<List<ArticleBean>?> = runCatching { api.loadTop().data }

  override fun download(url: String, path: String?): Flow<Int> = flow {
    val name = url.substring(url.lastIndexOf("/") + 1)
    val file = if (path == null) File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name) else File(path)
    val body = api.download(url, "bytes=${file.length()}-")
    val length = body.contentLength()
    withContext(Dispatchers.IO) {
      runCatching {
        file.sink(true).buffer().use { sink ->
          var size = file.length()
          if (size < length) {
            val buffer = body.byteStream().source().buffer()
            val bytes = ByteArray(length.toInt())
            while (size < length) {
              buffer.read(bytes).takeIf { it > -1 }?.also {
                size += it
                sink.write(bytes, 0, it)
                val progress = (1F * size * length / length).toInt()
                emit(progress)
              }
            }
            sink.flush()
          }
        }
      }
    }
  }
}
