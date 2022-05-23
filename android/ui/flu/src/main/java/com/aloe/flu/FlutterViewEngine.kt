package com.aloe.flu

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewTreeLifecycleOwner
import io.flutter.FlutterInjector
import io.flutter.embedding.android.ExclusiveAppComponent
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.platform.PlatformPlugin

class FlutterViewEngine(applicationContext: Context) : DefaultLifecycleObserver, ExclusiveAppComponent<Activity> {
    private var flutterView: FlutterView? = null
    private var platformPlugin: PlatformPlugin? = null
    private val engine: FlutterEngine = FlutterEngine(applicationContext)

    init {
        val path = FlutterInjector.instance().flutterLoader().findAppBundlePath()
        engine.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint(path, "showApp"))
    }

    private fun hookActivityAndView() {
        flutterView?.let { flutterView ->
            platformPlugin = PlatformPlugin(flutterView.context as Activity, engine.platformChannel)
            val owner = ViewTreeLifecycleOwner.get(flutterView)
                ?: throw NullPointerException("flutter view must bind LifecycleOwner")
            engine.activityControlSurface.attachToActivity(this, owner.lifecycle)
            flutterView.attachToFlutterEngine(engine)
            owner.lifecycle.addObserver(this)
        }
    }

    private fun unhookActivityAndView() {
        engine.activityControlSurface.detachFromActivity()
        platformPlugin?.destroy()
        platformPlugin = null
        engine.lifecycleChannel.appIsDetached()
        flutterView?.also {
            ViewTreeLifecycleOwner.get(it)?.lifecycle?.removeObserver(this)
            it.detachFromFlutterEngine()
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        engine.lifecycleChannel.appIsResumed()
        platformPlugin?.updateSystemUiOverlays()
    }

    override fun onPause(owner: LifecycleOwner) {
        engine.lifecycleChannel.appIsInactive()
    }

    override fun onStop(owner: LifecycleOwner) {
        engine.lifecycleChannel.appIsPaused()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        detachFlutterView()
    }

    fun onUserLeaveHint() {
        engine.activityControlSurface.takeUnless { flutterView == null }?.onUserLeaveHint()
    }

    override fun detachFromFlutterEngine() {
        (flutterView?.parent as? ViewGroup)?.removeView(flutterView)
        detachFlutterView()
    }

    override fun getAppComponent(): Activity = flutterView?.context as? Activity ?: throw ClassCastException("类型错误")

    fun attachFlutterView(flutterView: FlutterView) {
        this.flutterView = flutterView
        hookActivityAndView()
    }

    private fun detachFlutterView() {
        unhookActivityAndView()
        flutterView = null
    }
}
