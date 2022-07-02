package com.aloe.web

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.github.lzyzsd.jsbridge.BridgeWebView
import com.github.lzyzsd.jsbridge.BridgeWebViewClient
import java.io.FileInputStream

class AppWebView : BridgeWebView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun generateBridgeWebViewClient(): BridgeWebViewClient {
        return WebViewClient(this)
    }

    private class WebViewClient(webView: BridgeWebView) : BridgeWebViewClient(webView) {

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
}
