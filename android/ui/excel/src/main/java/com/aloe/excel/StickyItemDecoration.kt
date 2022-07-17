package com.aloe.excel

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StickyItemDecoration(private val stickyItem: Int) : RecyclerView.ItemDecoration() {
  /**
   * 悬浮Item距离顶部距离.
   */
  private var stickyItemMarginTop = 0

  /**
   * 绑定数据的位置.
   */
  private var bindDataIndex = -1

  /**
   * 当前悬浮Item的类型.
   */
  private var stickyViewType = -1

  /**
   * 上一个悬浮Item的高度.
   */
  private var prevStickyItemHeight = -3
  private val stickyIndexList = mutableListOf<Int>()
  private val mapStickyViewHolder = mutableMapOf<Int, RecyclerView.ViewHolder>()

  override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
    super.onDrawOver(c, parent, state)
    val adapter = parent.adapter
    val layoutManager = parent.layoutManager
    if (adapter == null || adapter.itemCount < 1 || layoutManager !is LinearLayoutManager) return
    var currUiFindStickyView = false
    val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
    stickyIndexList.takeIf { firstVisiblePosition == 0 }?.clear()
    var shouldSticky = false
    var index = 0
    while (index <= firstVisiblePosition && !shouldSticky) {
      shouldSticky = isStickyItem(adapter, index++)
    }
    for (i in 0 until parent.childCount) {
      val view = parent.getChildAt(i)
      index = layoutManager.getPosition(view)
      if (shouldSticky && isStickyItem(adapter, index)) {
        currUiFindStickyView = true
        if (prevStickyItemHeight == -3 || i == 0) {
          prevStickyItemHeight = view.height
          stickyViewType = adapter.getItemViewType(index)
        } else if (prevStickyItemHeight == view.height) {
          stickyViewType = adapter.getItemViewType(index)
        }
        if (mapStickyViewHolder[stickyViewType] == null) {
          mapStickyViewHolder[stickyViewType] = adapter.onCreateViewHolder(parent, stickyViewType)
        }
        val position = firstVisiblePosition + i
        stickyIndexList.takeUnless { stickyIndexList.contains(position) }?.add(position)
        if (view.top <= 0) {
          bindDataForStickyView(adapter, firstVisiblePosition, parent.measuredWidth)
        } else if (stickyIndexList.isNotEmpty()) {
          if (stickyIndexList.size == 1) {
            bindDataForStickyView(adapter, stickyIndexList[0], parent.measuredWidth)
          } else {
            val indexOfCurrPosition = stickyIndexList.lastIndexOf(position)
            takeIf { indexOfCurrPosition >= 1 }
              ?.bindDataForStickyView(adapter, stickyIndexList[indexOfCurrPosition - 1], parent.measuredWidth)
          }
        }
        mapStickyViewHolder[stickyViewType]?.also {
          if (view.top > 0 && view.top <= it.itemView.height) {
            stickyItemMarginTop = it.itemView.height - view.top
          } else {
            stickyItemMarginTop = 0
            val nextStickyView = getNextStickyView(adapter, layoutManager, parent)
            if (nextStickyView != null && nextStickyView.top <= it.itemView.height) {
              stickyItemMarginTop = it.itemView.height - nextStickyView.top
            }
          }
          drawStickyItemView(it, c)
        }
        break
      }
    }
    if (!currUiFindStickyView) {
      stickyItemMarginTop = 0
      if (firstVisiblePosition + parent.childCount == adapter.itemCount && stickyIndexList.isNotEmpty()) {
        bindDataForStickyView(adapter, stickyIndexList[stickyIndexList.size - 1], parent.measuredWidth)
      }
      mapStickyViewHolder[stickyViewType].takeIf { shouldSticky }?.also { drawStickyItemView(it, c) }
    }
  }

  private fun isStickyItem(adapter: RecyclerView.Adapter<*>, index: Int): Boolean =
    adapter.getItemViewType(index).and(stickyItem) != 0

  fun bindDataForStickyView(adapter: RecyclerView.Adapter<in RecyclerView.ViewHolder>, index: Int, width: Int) {
    val viewHolder = mapStickyViewHolder[adapter.getItemViewType(index)] ?: return
    bindDataIndex = index
    adapter.onBindViewHolder(viewHolder, bindDataIndex)
    if (!viewHolder.itemView.isLayoutRequested) return
    val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
    val layoutParams = viewHolder.itemView.layoutParams
    val heightSpec =
      if (layoutParams.height > 0) View.MeasureSpec.makeMeasureSpec(layoutParams.height, View.MeasureSpec.EXACTLY)
      else View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    viewHolder.itemView.measure(widthSpec, heightSpec)
    viewHolder.itemView.layout(0, 0, viewHolder.itemView.measuredWidth, viewHolder.itemView.measuredHeight)
  }

  private fun getNextStickyView(
    adapter: RecyclerView.Adapter<*>,
    layoutManager: LinearLayoutManager,
    recyclerView: RecyclerView
  ): View? {
    var index = 0
    var nextStickyView: View? = null
    for (i in 0 until recyclerView.childCount) {
      val view = recyclerView.getChildAt(i)
      if (isStickyItem(adapter, layoutManager.getPosition(view))) {
        nextStickyView = view
        index++
      }
      if (index == 2) break
    }
    return if (index > 1) nextStickyView else null
  }

  private fun drawStickyItemView(viewHolder: RecyclerView.ViewHolder, canvas: Canvas) {
    val count = canvas.save()
    canvas.translate(0F, -stickyItemMarginTop.toFloat())
    viewHolder.itemView.draw(canvas)
    canvas.restoreToCount(count)
  }

  fun clear() = mapStickyViewHolder.clear()
}
