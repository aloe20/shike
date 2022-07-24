package com.aloe.shike.ui

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aloe.shike.generic.LocalNavController
import com.aloe.shike.generic.lineModifier
import com.aloe.web.JsBridge
import com.google.accompanist.web.*
import java.io.FileInputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebLayout(url: String) {
  val context = LocalContext.current
  val showBridge = remember { Uri.parse(url).path == "/android_asset/bridge.html" }
  val state = rememberWebViewState(url = url)
  val navigator = rememberWebViewNavigator()
  val navController = LocalNavController.current
  Scaffold(modifier = Modifier.statusBarsPadding()) {
    Column(modifier = Modifier.fillMaxSize()) {
      val loadingState = state.loadingState
      val jsBridge = remember { JsBridge() }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
      ) {
        IconButton(
          onClick = { if (navigator.canGoBack) navigator.navigateBack() else navController.navigateUp() },
          modifier = Modifier.size(44.dp)
        ) {
          Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "")
        }
        Text(text = "WEB", modifier = Modifier.align(Alignment.Center))
        if (showBridge) {
          Text(
            text = "发送1",
            modifier = Modifier
              .align(Alignment.CenterEnd)
              .padding(end = 48.dp)
              .clickable {
                jsBridge.send("hello") {
                  Toast
                    .makeText(context, it ?: "", Toast.LENGTH_SHORT)
                    .show()
                }
              }
              .width(48.dp)
              .wrapContentHeight(),
            fontSize = 12.sp
          )
          Text(
            text = "发送2",
            modifier = Modifier
              .align(Alignment.CenterEnd)
              .clickable {
                jsBridge.callHandler("functionInJs", "{\"age\":19}") {
                  Toast
                    .makeText(context, it ?: "", Toast.LENGTH_SHORT)
                    .show()
                }
              }
              .width(48.dp)
              .wrapContentHeight(),
            fontSize = 12.sp
          )
        }
        Spacer(modifier = lineModifier().align(Alignment.BottomCenter))
        if (loadingState is LoadingState.Loading) {
          LinearProgressIndicator(
            progress = loadingState.progress,
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .align(Alignment.BottomCenter)
          )
        }
      }
      val webClient = remember { SimpleWebViewClient(jsBridge) }
      WebView(
        state = state,
        modifier = Modifier.weight(1f),
        navigator = navigator,
        onCreated = { webView ->
          webView.settings.javaScriptEnabled = true
          if (showBridge) {
            jsBridge.registerDefaultHandler { s, function ->
              function.invoke("收到web的消息$s")
            }.registerHandler("submitFromWeb") { data, block ->
              block.invoke("收到web的消息$data")
            }
          }
        },
        client = webClient
      )
    }
  }
}

private class SimpleWebViewClient(private val jsBridge: JsBridge) : AccompanistWebViewClient() {
  override fun onPageFinished(view: WebView?, url: String?) {
    jsBridge.onPageFinished(view)
    super.onPageFinished(view, url)
  }

  override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
    return jsBridge.shouldOverrideUrlLoading(view, request?.url?.toString()) || super.shouldOverrideUrlLoading(
      view,
      request
    )
  }

  override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
    if ("localhost" == request.url.host) {
      if ("/image" == request.url.path) {
        return request.url.getQueryParameter("resource")?.let {
          runCatching {
            val type = it.substring(it.lastIndexOf('.') + 1)
            WebResourceResponse("image/$type", Charsets.UTF_8.name(), FileInputStream(it))
          }.getOrElse {
            super.shouldInterceptRequest(view, request)
          }
        }
      }
    }
    return super.shouldInterceptRequest(view, request)
  }
}
