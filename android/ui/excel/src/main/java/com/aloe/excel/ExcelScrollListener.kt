package com.aloe.excel

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ExcelScrollListener : RecyclerView.OnScrollListener(), View.OnTouchListener {
  private val views = mutableListOf<RecyclerView>()
  private var scrollRv: RecyclerView? = null
  private var listener: (() -> Unit)? = null
  var scrollx = 0

  fun setListener(block: () -> Unit) {
    this.listener = block
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouch(v: View, event: MotionEvent): Boolean {
    val tmpView = v as? RecyclerView
    if (scrollRv !== v) {
      scrollRv?.also {
        it.stopScroll()
        it.removeOnScrollListener(this)
      }
      scrollRv = tmpView
      tmpView?.addOnScrollListener(this)
    }
    return false
  }

  override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
    scrollx += dx
    if (views.contains(recyclerView)) {
      views.forEach {
        if (it != recyclerView) {
          it.scrollBy(dx, dy)
          listener?.invoke()
        }
      }
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  fun addView(rv: RecyclerView) {
    rv.setOnTouchListener(this)
    views.add(rv)
  }

  fun contains(view: View): Boolean {
    return views.contains(view)
  }
}
