package com.aloe.web

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JsPromptResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

const val routerWebPrefix = "web?url="

@Composable
fun WebLayout(url: String = "http://192.168.1.4:3000/vue", click: (() -> Unit)? = null) {
    val webView = AppWebView(LocalContext.current)
    webView.webChromeClient = object : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            Log.d("js", "${consoleMessage?.message()}")
            return super.onConsoleMessage(consoleMessage)
        }

        override fun onJsPrompt(
            view: WebView?,
            url: String?,
            message: String?,
            defaultValue: String?,
            result: JsPromptResult
        ): Boolean {
            result.confirm()
            return super.onJsPrompt(view, url, message, defaultValue, result)
        }
    }
    /*with(webView.settings) {
        domStorageEnabled = true
        databaseEnabled = true
        allowFileAccess = true
        allowContentAccess = true
    }*/
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = "网页", modifier = Modifier.padding(0.dp), fontSize = 16.sp)
        }, modifier = Modifier.height(40.dp), navigationIcon = {
            IconButton(onClick = { click?.invoke() }, modifier = Modifier.padding(0.dp)) {
                Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "", tint = Color.White)
            }
        }, actions = {
            IconButton(onClick = { webView.send("hello") { Log.d("js", it) } }) {
                Text(text = "发送1", fontSize = 12.sp)
            }
            IconButton(onClick = { webView.callHandler("functionInJs", "{\"age\":19}") { Log.d("js", it) } }) {
                Text(text = "发送2", fontSize = 12.sp)
            }
        })
    }, modifier = Modifier.background(Color.Red)) { padding ->
        AndroidView(factory = { webView }, modifier = Modifier.padding(padding)) {
            with(it) {
                registerHandler("submitFromWeb") { data, block ->
                    Log.d("js", "handler = submitFromWeb, data from web = $data")
                    block.onCallBack("response data from Android")
                }
                loadUrl(url)
            }
        }
    }
}
