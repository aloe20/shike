package com.aloe.web

import android.content.Context
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.webkit.WebView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.URLDecoder
import org.json.JSONArray
import org.json.JSONObject

class JsBridge {
  private var uniqueId: Long = 0
  private var startupMessage: MutableList<Message>? = ArrayList()
  private var defaultHandler: ((String?, (String) -> Unit) -> Unit)? = null
  private var responseCallbacks: MutableMap<String?, (String?) -> Unit> = mutableMapOf()
  private var messageHandlers: MutableMap<String?, (String?, (String) -> Unit) -> Unit> = mutableMapOf()
  private var refWebView: WeakReference<WebView?> = WeakReference(null)

  fun onPageFinished(view: WebView?) {
    view?.also {
      refWebView = WeakReference(it)
      webViewLoadLocalJs(it, "JsBridge.js")
      if (startupMessage != null) {
        for (m in startupMessage!!) {
          dispatchMessage(it, m)
        }
        startupMessage = null
      }
    }
  }

  fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
    val url2 = runCatching { URLDecoder.decode(url, "UTF-8") }.getOrNull()
    return if (url2 == null) false
    else if (url2.startsWith(YY_RETURN_DATA)) true.apply { handlerReturnData(url2) }
    else if (url2.startsWith(YY_OVERRIDE_SCHEMA)) true.apply { view?.also { flushMessageQueue(it) } }
    else false
  }

  private fun dispatchMessage(view: WebView, m: Message) {
    var messageJson = m.toJson()
    //escape special characters for json string
    messageJson = messageJson.replace("(\\\\)([^utrn])".toRegex(), "\\\\\\\\$1$2")
    messageJson = messageJson.replace("(?<=[^\\\\])(\")".toRegex(), "\\\\\"")
    val javascriptCommand = String.format(JS_HANDLE_MESSAGE_FROM_JAVA, messageJson)
    if (Thread.currentThread() === Looper.getMainLooper().thread) view.loadUrl(javascriptCommand)
  }

  @Suppress("SameParameterValue")
  private fun webViewLoadLocalJs(view: WebView, path: String) {
    val jsContent = assetFile2Str(view.context, path)
    view.loadUrl("javascript:$jsContent")
  }

  private fun assetFile2Str(c: Context, urlStr: String): String {
    return c.assets.open(urlStr).use {
      val bufferedReader = BufferedReader(InputStreamReader(it))
      var line: String?
      val sb = StringBuilder()
      do {
        line = bufferedReader.readLine()
        if (line != null && !line.matches(Regex("^\\s*\\/\\/.*"))) sb.append(line)
      } while (line != null)
      bufferedReader.close()
      sb.toString()
    }
  }

  private fun handlerReturnData(url: String?) {
    val functionName = getFunctionFromReturnUrl(url!!)
    val f = responseCallbacks[functionName]
    val data = getDataFromReturnUrl(url)
    if (f != null) {
      f.invoke(data)
      responseCallbacks.remove(functionName)
      return
    }
  }

  private fun getDataFromReturnUrl(url: String): String? {
    return if (url.startsWith(YY_FETCH_QUEUE)) url.replace(YY_FETCH_QUEUE, EMPTY_STR)
    else {
      val temp = url.replace(YY_RETURN_DATA, EMPTY_STR)
      val functionAndData = temp.split(SPLIT_MARK.toRegex()).toTypedArray()
      if (functionAndData.size >= 2) StringBuilder().run {
        for (i in 1 until functionAndData.size) {
          append(functionAndData[i])
        }
      }.toString() else null
    }
  }

  private fun getFunctionFromReturnUrl(url: String): String? {
    val temp = url.replace(YY_RETURN_DATA, EMPTY_STR)
    val functionAndData = temp.split(SPLIT_MARK.toRegex()).toTypedArray()
    return if (functionAndData.isNotEmpty()) functionAndData[0] else null
  }

  private fun flushMessageQueue(view: WebView) {
    if (Thread.currentThread() === Looper.getMainLooper().thread) {
      loadUrl(view, JS_FETCH_QUEUE_FROM_JAVA) {
        // deserializeMessage
        val list: List<Message>? = runCatching { Message.toArrayList(it) }.getOrNull()
        if (!list.isNullOrEmpty()) {
          for (i in list.indices) {
            val m = list[i]
            val responseId = m.responseId
            // 是否是response
            if (responseId.isNullOrEmpty()) {
              // if had callbackId
              val callbackId = m.callbackId
              if (m.handlerName.isNullOrEmpty()) {
                defaultHandler
              } else {
                messageHandlers[m.handlerName]
              }?.takeUnless { callbackId.isNullOrEmpty() }?.invoke(m.data) { data ->
                val responseMsg = Message()
                responseMsg.responseId = callbackId
                responseMsg.responseData = data
                queueMessage(view, responseMsg)
              }
            } else {
              val function = responseCallbacks[responseId]
              function?.invoke(m.responseData)
              responseCallbacks.remove(responseId)
            }
          }
        }
      }
    }
  }

  private fun queueMessage(view: WebView, m: Message) {
    startupMessage?.add(m) ?: dispatchMessage(view, m)
  }

  private fun parseFunctionName(jsUrl: String): String {
    return jsUrl.replace("javascript:WebViewJavascriptBridge.", "").replace("\\(.*\\);".toRegex(), "")
  }

  fun registerDefaultHandler(block: (String?, (String) -> Unit) -> Unit): JsBridge = apply {
    defaultHandler = block
  }

  fun registerHandler(handlerName: String?, handler: ((String?, (String) -> Unit) -> Unit)) {
    messageHandlers[handlerName] = handler
  }

  fun webViewLoadJs(view: WebView, url: String) {
    var js = "var newscript = document.createElement(\"script\");"
    js += "newscript.src=\"$url\";"
    js += "document.scripts[0].parentNode.insertBefore(newscript,document.scripts[0]);"
    view.loadUrl("javascript:$js")
  }

  @Suppress("SameParameterValue")
  private fun loadUrl(view: WebView, jsUrl: String, returnCallback: (String?) -> Unit) {
    view.loadUrl(jsUrl)
    responseCallbacks[parseFunctionName(jsUrl)] = returnCallback
  }

  fun callHandler(handlerName: String?, data: String?, callBack: (String?) -> Unit) {
    doSend(handlerName, data, callBack)
  }

  fun send(data: String?) {
    send(data, null)
  }

  fun send(data: String?, responseCallback: ((String?) -> Unit)?) {
    doSend(null, data, responseCallback)
  }

  private fun doSend(handlerName: String?, data: String?, responseCallback: ((String?) -> Unit)?) {
    val m = Message()
    m.takeUnless { data.isNullOrEmpty() }?.data = data
    if (!TextUtils.isEmpty(data)) {
      m.data = data
    }
    if (responseCallback != null) {
      val callbackStr = String.format(
        CALLBACK_ID_FORMAT,
        (++uniqueId).toString() + (UNDERLINE_STR + SystemClock.currentThreadTimeMillis())
      )
      responseCallbacks[callbackStr] = responseCallback
      m.callbackId = callbackStr
    }
    m.takeUnless { handlerName.isNullOrEmpty() }?.handlerName = handlerName
    refWebView.get()?.also { queueMessage(it, m) }
  }

  companion object {
    const val YY_OVERRIDE_SCHEMA = "yy://"
    const val YY_RETURN_DATA = YY_OVERRIDE_SCHEMA + "return/" //格式为   yy://return/{function}/returncontent
    const val YY_FETCH_QUEUE = YY_RETURN_DATA + "_fetchQueue/"
    const val EMPTY_STR = ""
    const val UNDERLINE_STR = "_"
    const val SPLIT_MARK = "/"
    const val CALLBACK_ID_FORMAT = "JAVA_CB_%s"
    const val JS_FETCH_QUEUE_FROM_JAVA = "javascript:WebViewJavascriptBridge._fetchQueue();"
    const val JS_HANDLE_MESSAGE_FROM_JAVA = "javascript:WebViewJavascriptBridge._handleMessageFromNative('%s');"
  }
}

private data class Message(
  var callbackId: String? = null,
  var responseId: String? = null,
  var responseData: String? = null,
  var data: String? = null,
  var handlerName: String? = null
) {

  fun toJson(): String = JSONObject().run {
    put(CALLBACK_ID_STR, callbackId)
    put(DATA_STR, data)
    put(HANDLER_NAME_STR, handlerName)
    put(RESPONSE_DATA_STR, responseData)
    put(RESPONSE_ID_STR, responseId)
    toString()
  }

  companion object {
    private const val CALLBACK_ID_STR = "callbackId"
    private const val RESPONSE_ID_STR = "responseId"
    private const val RESPONSE_DATA_STR = "responseData"
    private const val DATA_STR = "data"
    private const val HANDLER_NAME_STR = "handlerName"
    private fun JSONObject.toMessage(): Message = Message(
      callbackId = if (has(CALLBACK_ID_STR)) getString(CALLBACK_ID_STR) else null,
      responseId = if (has(RESPONSE_ID_STR)) getString(RESPONSE_ID_STR) else null,
      responseData = if (has(RESPONSE_DATA_STR)) getString(RESPONSE_DATA_STR) else null,
      data = if (has(DATA_STR)) getString(DATA_STR) else null,
      handlerName = if (has(HANDLER_NAME_STR)) getString(HANDLER_NAME_STR) else null
    )

    fun toArrayList(jsonStr: String?): List<Message> {
      val list: MutableList<Message> = mutableListOf()
      list.runCatching {
        val jsonArray = JSONArray(jsonStr)
        for (i in 0 until jsonArray.length()) {
          add(jsonArray.getJSONObject(i).toMessage())
        }
      }
      return list
    }
  }
}

