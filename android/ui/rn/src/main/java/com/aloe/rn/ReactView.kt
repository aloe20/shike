package com.aloe.rn

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.MainThread
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.facebook.infer.annotation.Assertions
import com.facebook.react.*
import com.facebook.react.BuildConfig
import com.facebook.react.bridge.*
import com.facebook.react.common.LifecycleState
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler
import com.facebook.react.modules.network.OkHttpClientProvider
import com.facebook.react.shell.MainReactPackage
import com.facebook.react.uimanager.ViewManager
import java.io.File
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okio.buffer
import okio.sink

class ReactView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
  ReactRootView(context, attrs, defStyle) {
  private var activity: FragmentActivity? = context as? FragmentActivity
  private var backCallback: (() -> Unit)? = null
  private val btnHandler = DefaultHardwareBackBtnHandler {
    reactInstanceManager?.onBackPressed() ?: backCallback?.invoke()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    findViewTreeLifecycleOwner()?.also { owner ->
      owner.lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
          activity?.also {
            when (event) {
              Lifecycle.Event.ON_RESUME -> reactInstanceManager?.onHostResume(it, btnHandler)
              Lifecycle.Event.ON_PAUSE -> reactInstanceManager?.onHostPause(it)
              Lifecycle.Event.ON_DESTROY -> {
                reactInstanceManager?.onHostDestroy(it)
                unmountReactApplication()
              }
              else -> Unit
            }
          }
        }
      })
      activity?.onBackPressedDispatcher?.addCallback(owner, object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
          reactInstanceManager?.onBackPressed()
        }
      })
    }
  }

  override fun onViewAdded(child: View?) {
    super.onViewAdded(child)
    (child as? TextView)?.addTextChangedListener(afterTextChanged = {
      Log.d("aloe", "addTextChangedListener: $it")
      Thread.dumpStack()
    })
    Log.d("aloe", "onViewAdded: ${child?.javaClass}")
  }

  override fun onViewRemoved(child: View?) {
    super.onViewRemoved(child)
    Log.d("aloe", "onViewRemoved: ${child?.javaClass}")
  }

  /**
   * assets://index.bundle
   * file://sdcard/myapp_cache/index.bundle
   * http://localhost:8081/rn/index.bundle
   */
  @MainThread
  fun loadPage(jsBundle: Uri) {
    if (jsBundle.scheme == "http" || jsBundle.scheme == "https") {
      val dir = File(context.filesDir, "bundle")
      if (!dir.exists()) {
        dir.mkdirs()
      }
      val name = jsBundle.lastPathSegment ?: "index.bundle"
      val file = File(dir, name)
      if (file.exists() && file.length() > 0) {
        realLoadPage(Uri.fromFile(file))
      } else {
        OkHttpClientProvider.getOkHttpClient().newCall(Request.Builder().url(getUrl(jsBundle)).build())
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
                post { realLoadPage(Uri.fromFile(file)) }
              }
            }
          })
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

  private fun getUrl(uri: Uri): String =
    uri.buildUpon().appendQueryParameter("platform", "android")
      .appendQueryParameter("dev", BuildConfig.DEBUG.toString())
      .appendQueryParameter("app", context.packageName)
      .appendQueryParameter("minify", true.toString()).build().toString()

  fun setBackBtnHandler(callback: () -> Unit) = apply { backCallback = callback }

  companion object {
    private class ReactHost(private val app: Application, private val jsBundle: Uri?) : ReactNativeHost(app) {
      override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG

      override fun getPackages(): MutableList<ReactPackage> = mutableListOf(MainReactPackage(), RnReactPackage())

      override fun getJSMainModuleName(): String = "index"

      override fun getBundleAssetName(): String = "index.bundle"

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
