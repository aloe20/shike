package com.aloe.rn

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import androidx.activity.OnBackPressedCallback
import androidx.annotation.MainThread
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.facebook.react.BuildConfig
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.ReactRootView
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler
import com.facebook.react.modules.network.OkHttpClientProvider
import com.facebook.react.shell.MainReactPackage
import com.facebook.react.uimanager.ViewManager
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okio.buffer
import okio.sink

class ReactView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    ReactRootView(context, attrs, defStyle), LifecycleEventObserver, DefaultHardwareBackBtnHandler {
    private var activity: FragmentActivity? = context as? FragmentActivity
    private var backCallback: (() -> Unit)? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewTreeLifecycleOwner()?.also {
            it.lifecycle.addObserver(this)
            activity?.onBackPressedDispatcher?.addCallback(it, object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    reactInstanceManager?.onBackPressed()
                }
            })
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        activity?.also {
            when (event) {
                Lifecycle.Event.ON_RESUME -> reactInstanceManager?.onHostResume(it, this)
                Lifecycle.Event.ON_PAUSE -> reactInstanceManager?.onHostPause(it)
                Lifecycle.Event.ON_DESTROY -> {
                    reactInstanceManager?.onHostDestroy(it)
                    unmountReactApplication()
                }
                else -> Unit
            }
        }
    }

    override fun invokeDefaultOnBackPressed() {
        if (reactInstanceManager == null) {
            backCallback?.invoke()
        } else {
            reactInstanceManager?.onBackPressed()
        }
    }

    /**
     * assets://index.android.bundle
     * file://sdcard/myapp_cache/index.android.bundle
     * http://host/index.android.bundle
     */
    @MainThread
    fun loadPage(scope: CoroutineScope? = null, jsBundle: Uri?) {
        if (jsBundle != null && (jsBundle.scheme == "http" || jsBundle.scheme == "https")) {
            val dir = File(context.filesDir, "bundle")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val name = jsBundle.lastPathSegment ?: "index.bundle"
            val file = File(dir, name)
            if (file.exists() && file.length() > 0) {
                realLoadPage(Uri.fromFile(file))
            } else {
                scope?.launch(Dispatchers.IO) {
                    OkHttpClientProvider.getOkHttpClient().newCall(Request.Builder().url(jsBundle.toString()).build())
                        .enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                e.printStackTrace()
                            }

                            override fun onResponse(call: Call, response: Response) {
                                if (response.isSuccessful && response.code == 200) {
                                    if (!file.exists()) {
                                        file.createNewFile()
                                    }
                                    response.body?.source()?.also { source ->
                                        file.sink().buffer().also { sink ->
                                            sink.writeAll(source)
                                            sink.flush()
                                        }
                                    }
                                    launch(Dispatchers.Main) { realLoadPage(Uri.fromFile(file)) }
                                }
                            }
                        })
                }
            }
        } else {
            realLoadPage(jsBundle)
        }
    }

    private fun realLoadPage(jsBundle: Uri?) {
        reactInstanceManager?.onHostDestroy(activity)
        unmountReactApplication()
        startReactApplication(
            ReactHost(context.applicationContext as Application, jsBundle).reactInstanceManager, "rn", null
        )
    }

    fun setBackBtnHandler(callback: () -> Unit) = apply { backCallback = callback }

    companion object {
        private class ReactHost(application: Application, private val jsBundle: Uri?) : ReactNativeHost(application) {
            override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG

            override fun getPackages(): MutableList<ReactPackage> = mutableListOf(MainReactPackage(), RnReactPackage())

            override fun getJSMainModuleName(): String = "index"

            override fun getJSBundleFile(): String? = if (jsBundle == null) null else when (jsBundle.scheme) {
                "assets" -> jsBundle.toString()
                "file" -> jsBundle.path
                else -> null
            }
        }

        private class RnReactPackage : ReactPackage {
            override fun createNativeModules(reactContext: ReactApplicationContext): MutableList<NativeModule> =
                mutableListOf(ReactToast(reactContext))

            override fun createViewManagers(reactContext: ReactApplicationContext): MutableList<ViewManager<*, *>> =
                mutableListOf(ReactText())
        }
    }
}
