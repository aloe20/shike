package com.aloe.http

import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okhttp3.internal.platform.Platform
import okio.Buffer
import okio.GzipSource

/**
 * OkHttp日志拦截器.
 * @author Aloe
 * @property logger 日志打印
 */
internal class HttpLoggingInterceptor @JvmOverloads constructor(
  private val logger: Logger = Logger.DEFAULT
) : Interceptor {

  @Volatile
  private var headersToRedact = emptySet<String>()

  @set:JvmName("level")
  @Volatile
  var level = Level.NONE

  enum class Level {
    NONE, HEADERS, BODY
  }

  fun interface Logger {
    fun log(message: String)

    companion object {
      @JvmField
      val DEFAULT: Logger = DefaultLogger()

      private class DefaultLogger : Logger {
        override fun log(message: String) {
          Platform.get().log(message)
        }
      }
    }
  }

  fun setLevel(level: Level) = apply {
    this.level = level
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val level = this.level
    val request = chain.request()
    if (level == Level.NONE || request.header("noLog") != null) {
      return chain.proceed(request)
    }

    val logBody = level == Level.BODY
    val logHeaders = logBody || level == Level.HEADERS

    val requestBody = request.body

    val connection = chain.connection()
    var requestStartMessage =
      ("--> ${request.method} ${request.url}${if (connection != null) " " + connection.protocol() else ""}")
    if (!logHeaders && requestBody != null) {
      requestStartMessage += " (${requestBody.contentLength()}-byte body)"
    }
    logger.log(requestStartMessage)

    if (logHeaders) {
      val headers = request.headers

      if (requestBody != null) {
        requestBody.contentType()?.let {
          if (headers["Content-Type"] == null) {
            logger.log("Content-Type: $it")
          }
        }
        if (requestBody.contentLength() != -1L) {
          if (headers["Content-Length"] == null) {
            logger.log("Content-Length: ${requestBody.contentLength()}")
          }
        }
      }

      for (i in 0 until headers.size) {
        logHeader(headers, i)
      }

      if (!logBody || requestBody == null) {
        logger.log("--> END ${request.method}")
      } else if (bodyHasUnknownEncoding(request.headers)) {
        logger.log("--> END ${request.method} (encoded body omitted)")
      } else if (requestBody.isDuplex()) {
        logger.log("--> END ${request.method} (duplex request body omitted)")
      } else if (requestBody.isOneShot()) {
        logger.log("--> END ${request.method} (one-shot body omitted)")
      } else {
        val buffer = Buffer()
        requestBody.writeTo(buffer)

        val contentType = requestBody.contentType()
        val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

        logger.log("")
        if (buffer.isProbablyUtf8()) {
          logger.log(buffer.readString(charset))
          logger.log("--> END ${request.method} (${requestBody.contentLength()}-byte body)")
        } else {
          logger.log(
            "--> END ${request.method} (binary ${requestBody.contentLength()}-byte body omitted)"
          )
        }
      }
    }

    val startNs = System.nanoTime()
    val response: Response = run { chain.proceed(request) }

    val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

    val responseBody = response.body!!
    val contentLength = responseBody.contentLength()
    val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
    val logTxt = "<-- ".plus(response.code).plus(if (response.message.isEmpty()) "" else ' ' + response.message)
      .plus(' ').plus(response.request.url).plus(" (").plus(tookMs).plus("ms")
      .plus(if (!logHeaders) ", $bodySize body" else "").plus(')')
    logger.log(logTxt)

    if (logHeaders) {
      val headers = response.headers
      for (i in 0 until headers.size) {
        logHeader(headers, i)
      }

      if (!logBody || !response.promisesBody()) {
        logger.log("<-- END HTTP")
      } else if (bodyHasUnknownEncoding(response.headers)) {
        logger.log("<-- END HTTP (encoded body omitted)")
      } else {
        val source = responseBody.source()
        source.request(Long.MAX_VALUE) // Buffer the entire body.
        var buffer = source.buffer

        var gzippedLength: Long? = null
        if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
          gzippedLength = buffer.size
          GzipSource(buffer.clone()).use { gzippedResponseBody ->
            buffer = Buffer()
            buffer.writeAll(gzippedResponseBody)
          }
        }

        val contentType = responseBody.contentType()
        val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

        if (!buffer.isProbablyUtf8()) {
          logger.log("")
          logger.log("<-- END HTTP (binary ${buffer.size}-byte body omitted)")
          return response
        }

        if (contentLength != 0L) {
          logger.log("")
          logger.log(buffer.clone().readString(charset))
        }

        if (gzippedLength != null) {
          logger.log("<-- END HTTP (${buffer.size}-byte, $gzippedLength-gzipped-byte body)")
        } else {
          logger.log("<-- END HTTP (${buffer.size}-byte body)")
        }
      }
    }

    return response
  }

  private fun logHeader(headers: Headers, i: Int) {
    val value = if (headers.name(i) in headersToRedact) "██" else headers.value(i)
    logger.log(headers.name(i) + ": " + value)
  }

  private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
    val contentEncoding = headers["Content-Encoding"] ?: return false
    return !contentEncoding.equals("identity", ignoreCase = true) &&
      !contentEncoding.equals("gzip", ignoreCase = true)
  }
}

fun Buffer.isProbablyUtf8(): Boolean {
  return runCatching {
    val prefix = Buffer()
    val byteCount = size.coerceAtMost(64)
    copyTo(prefix, 0, byteCount)
    var result = true
    for (i in 0 until 16) {
      if (prefix.exhausted()) {
        break
      }
      val codePoint = prefix.readUtf8CodePoint()
      if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
        result = false
      }
    }
    result
  }.getOrDefault(false)
}
