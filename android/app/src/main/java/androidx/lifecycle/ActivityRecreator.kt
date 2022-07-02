package androidx.lifecycle

import android.app.Activity
import com.aloe.shike.app.App
import java.lang.ref.WeakReference

internal object ActivityRecreator : EmptyActivityLifecycleCallbacks(), DefaultLifecycleObserver {
  private var appVisibility = false
  private var topActivity: WeakReference<Activity>? = null
  fun registerActivityLifecycle(app: App) {
    ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    app.registerActivityLifecycleCallbacks(this)
  }

  fun getTopActivity(): Activity? = topActivity?.takeIf { appVisibility }?.get()

  override fun onStart(owner: LifecycleOwner) {
    appVisibility = true
  }

  override fun onStop(owner: LifecycleOwner) {
    appVisibility = false
  }

  override fun onActivityResumed(activity: Activity) {
    topActivity?.clear()
    topActivity = WeakReference(activity)
  }
}
