package com.aloe.excel

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator

/**
 * 获取一屏数据进行订阅.
 * 注意：首次会加载第二屏的数据，并移出
 */
class RecyclerViewProxy(mainRecycler: RecyclerView, subRecycler: RecyclerView? = null) {
  private var lastPosition = -2
  private var hasScroll = false
  private val subscribe = mutableListOf<Int>()
  private val callbackData = mutableListOf<Int>()
  private var callback: ((List<Int>?, List<Int>?) -> Unit)? = null

  init {
    addChangeListener(mainRecycler)
    (mainRecycler.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    val scrollListener = getScrollListener()
    mainRecycler.addOnScrollListener(scrollListener)
    subRecycler?.also {
      (it.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
      it.addOnScrollListener(scrollListener)
    }
  }

  fun setCallback(callback: (List<Int>?, List<Int>?) -> Unit) {
    this.callback = callback
  }

  /**
   * 获取当前列表显示位置，用于取消订阅数据。点击刷新，显示并订阅第一页的数据(根据返回集合的size订阅第一页)，取消订阅之前显示的数据
   * @param reset 是否重置成第一页的位置
   * @return 当前列表位置集合
   */
  fun getCurSubData(reset: Boolean): List<Int> {
    callbackData.clear()
    if (subscribe.isNotEmpty()) {
      if (reset) {
        subscribe.forEachIndexed { index, data ->
          callbackData.add(data)
          subscribe[index] = index
        }
      } else {
        callbackData.addAll(subscribe)
      }
    }
    return callbackData
  }

  private fun addChangeListener(mainRecycler: RecyclerView) {
    mainRecycler.addOnChildAttachStateChangeListener(object :
      RecyclerView.OnChildAttachStateChangeListener {

      override fun onChildViewAttachedToWindow(view: View) {
        if (!hasScroll) {
          val position = mainRecycler.getChildAdapterPosition(view)
          if (!subscribe.contains(position) && position != RecyclerView.NO_POSITION) {
            (mainRecycler.layoutManager as? LinearLayoutManager)?.apply {
              val lastIndex = findLastVisibleItemPosition()
              if (lastPosition == lastIndex || position > lastIndex + 1) {
                return
              }
              lastPosition = lastIndex
            }
            subscribe.add(position)
            callbackData.clear()
            callbackData.add(position)
            callback?.invoke(callbackData, null)
          }
        }
      }

      override fun onChildViewDetachedFromWindow(view: View) {
        val position = mainRecycler.getChildAdapterPosition(view)
        if (subscribe.contains(position)) {
          subscribe.remove(position)
          callbackData.clear()
          callbackData.add(position)
          callback?.invoke(null, callbackData)
        }
      }
    })
  }

  private fun getScrollListener(): RecyclerView.OnScrollListener {
    return object : RecyclerView.OnScrollListener() {
      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        hasScroll = true
        if (newState != RecyclerView.SCROLL_STATE_SETTLING) {
          (recyclerView.layoutManager as? LinearLayoutManager)?.apply {
            val first = findFirstVisibleItemPosition()
            val last = findLastVisibleItemPosition()
            callbackData.clear()
            for (index in first..last) {
              if (index != RecyclerView.NO_POSITION) {
                callbackData.add(index)
              }
            }
            callbackData.removeAll(subscribe)
            if (callbackData.isNotEmpty()) {
              subscribe.addAll(callbackData)
              callback?.invoke(callbackData, null)
            }
          }
        }
      }
    }
  }
}
