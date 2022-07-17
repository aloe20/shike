package com.aloe.rn

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.facebook.react.uimanager.ReactStylesDiffMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class ReactText : SimpleViewManager<AppCompatTextView>() {
  override fun getName(): String = "RnText"

  override fun createViewInstance(reactContext: ThemedReactContext): AppCompatTextView {
    return AppCompatTextView(reactContext).apply {
      layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
  }

  override fun updateProperties(viewToUpdate: AppCompatTextView, props: ReactStylesDiffMap?) {
    super.updateProperties(viewToUpdate, props)
  }

  @ReactProp(name = "text")
  fun setText(view: AppCompatTextView, txt: String?) {
    view.text = txt
  }
}
