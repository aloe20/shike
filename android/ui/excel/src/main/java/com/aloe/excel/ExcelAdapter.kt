package com.aloe.excel

import android.graphics.Color
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlin.math.floor

abstract class ExcelAdapter<T, B, VH : RecyclerView.ViewHolder>(val size: Rect) : RecyclerView.Adapter<VH>() {
  private var width = 0
  protected val data = mutableListOf<B>()
  protected val topData = mutableListOf<T>()
  private val scrollListener = ExcelScrollListener()
  private val pool = RecyclerView.RecycledViewPool()
  private var excelClickListener: ((View, Int, Int) -> Unit)? = null
  private val itemDecoration: StickyItemDecoration = StickyItemDecoration(ITEM_TYPE_TOP)
  fun setExcelClickListener(listener: (View, Int, Int) -> Unit) {
    excelClickListener = listener
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    var layoutManager = recyclerView.layoutManager as? LinearLayoutManager
    if (layoutManager == null) {
      layoutManager = LinearLayoutManager(recyclerView.context)
    }
    recyclerView.layoutManager = layoutManager
    recyclerView.addItemDecoration(itemDecoration)
    recyclerView.post { width = recyclerView.measuredWidth }
    recyclerView.takeUnless { excelClickListener == null }?.addOnItemTouchListener(TouchListener())
    scrollListener.setListener { takeUnless { recyclerView.isComputingLayout }?.notifyItemChanged(0) }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    val linearLayout = LinearLayout(parent.context)
    linearLayout.isClickable = true
    val viewHolder = createHolder(linearLayout)
    if (viewType == ITEM_TYPE_TOP) {
      linearLayout.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, size.top)
      linearLayout.addView(createView(linearLayout, ViewType.START_TOP), LinearLayout.LayoutParams(size.left, size.top))
      val recyclerView = RecyclerView(parent.context)
      val layoutManager = LinearLayoutManager(parent.context, LinearLayoutManager.HORIZONTAL, false)
      recyclerView.layoutManager = layoutManager
      recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
      (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
      recyclerView.adapter = TopAdapter<T>()
      val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, size.top)
      linearLayout.addView(recyclerView, params)
      linearLayout.setBackgroundColor(Color.WHITE)
      scrollListener.addView(recyclerView)
    } else {
      var params = LinearLayout.LayoutParams(size.left, size.bottom - size.top)
      linearLayout.addView(createView(linearLayout, ViewType.START_BOTTOM), params)
      val recyclerView = RecyclerView(parent.context)
      recyclerView.setRecycledViewPool(pool)
      recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
      (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
      recyclerView.layoutManager = LinearLayoutManager(parent.context, LinearLayoutManager.HORIZONTAL, false)
      recyclerView.adapter = BottomAdapter()
      params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, size.bottom - size.top)
      linearLayout.addView(recyclerView, params)
      scrollListener.addView(recyclerView)
    }
    return viewHolder
  }

  @Suppress("UNCHECKED_CAST")
  override fun onBindViewHolder(holder: VH, position: Int) {
    val itemViewType = getItemViewType(position)
    if (itemViewType == ITEM_TYPE_TOP) {
      val adapter = ((holder.itemView as? ViewGroup)?.getChildAt(1) as? RecyclerView)?.adapter
      (adapter as? TopAdapter<T>)?.setData(topData)
      convertItem(holder, RecyclerView.NO_POSITION, RecyclerView.NO_POSITION)
    } else if (itemViewType == ITEM_TYPE_BOTTOM) {
      val limit = if (topData.isEmpty()) 0 else 1
      convertItem(holder, position - limit, RecyclerView.NO_POSITION)
      ((holder.itemView as? ViewGroup)?.getChildAt(1) as? RecyclerView)?.also {
        (it.adapter as ExcelAdapter<T, B, VH>.BottomAdapter).also { adapter ->
          val index = holder.absoluteAdapterPosition
          if (index != RecyclerView.NO_POSITION) {
            adapter.setIndex(index - limit)
            adapter.notifyItemRangeChanged(0, adapter.itemCount)
          }
        }
        (it.layoutManager as LinearLayoutManager).also { manager ->
          val index = scrollListener.scrollx / (size.right - size.left)
          val offset = scrollListener.scrollx - index * (size.right - size.left)
          manager.scrollToPositionWithOffset(index, -offset)
        }
      }
    }
  }

  override fun getItemViewType(position: Int): Int = if (position == 0) ITEM_TYPE_TOP else ITEM_TYPE_BOTTOM

  override fun getItemCount(): Int = if (topData.isEmpty()) data.size else data.size + 1

  fun setTopData(data: List<T>?) {
    topData.clear()
    if (!data.isNullOrEmpty()) {
      topData.addAll(data)
    }
    notifyItemChanged(0)
  }

  @Suppress("UNCHECKED_CAST")
  fun updateSticky() {
    itemDecoration.bindDataForStickyView(this as RecyclerView.Adapter<RecyclerView.ViewHolder>, 0, width)
  }

  fun addData(data: List<B>?, clear: Boolean = true) {
    if (clear) {
      this.data.clear()
    }
    if (!data.isNullOrEmpty()) {
      this.data.addAll(data)
    }
    notifyItemRangeChanged(0, itemCount)
  }

  abstract fun createHolder(itemView: View): VH

  abstract fun createView(parent: ViewGroup, viewType: ViewType): View

  abstract fun convertItem(viewHolder: VH, row: Int, column: Int)

  private inner class TopAdapter<T> : RecyclerView.Adapter<VH>() {
    private val data = mutableListOf<T>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
      createHolder(createView(parent, ViewType.END_TOP).apply {
        layoutParams = RecyclerView.LayoutParams(size.right - size.left, size.top)
      })

    override fun onBindViewHolder(holder: VH, position: Int) = convertItem(holder, RecyclerView.NO_POSITION, position)

    override fun getItemCount(): Int = data.size

    fun setData(data: List<T>?) {
      this.data.clear()
      if (!data.isNullOrEmpty()) {
        this.data.addAll(data)
      }
      notifyItemRangeChanged(0, itemCount)
    }
  }

  private inner class BottomAdapter : RecyclerView.Adapter<VH>() {
    private var indexRow = RecyclerView.NO_POSITION
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
      createHolder(createView(parent, ViewType.END_BOTTOM).apply {
        layoutParams = RecyclerView.LayoutParams(size.right - size.left, size.bottom - size.top)
      })

    override fun onBindViewHolder(holder: VH, position: Int) {
      this@ExcelAdapter.takeUnless { indexRow == RecyclerView.NO_POSITION }?.convertItem(holder, indexRow, position)
    }

    override fun getItemCount(): Int = topData.size

    fun setIndex(row: Int) {
      indexRow = row
    }
  }

  private inner class TouchListener : RecyclerView.SimpleOnItemTouchListener() {
    private var detector: GestureDetectorCompat? = null
    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean = false.apply {
      if (detector == null) {
        detector = GestureDetectorCompat(rv.context, object : GestureDetector.SimpleOnGestureListener() {
          override fun onSingleTapUp(e: MotionEvent): Boolean = false.apply {
            rv.findChildViewUnder(e.x, e.y)?.also { v ->
              val column = floor((scrollListener.scrollx + e.x - size.left) / (size.right - size.left)).toInt()
              when {
                e.y > size.top -> rv.getChildViewHolder(v).absoluteAdapterPosition.also { index ->
                  excelClickListener?.takeUnless { index == RecyclerView.NO_POSITION }
                    ?.invoke(v, index - (if (topData.isEmpty()) 0 else 1), column)
                }
                e.x > size.left -> excelClickListener?.invoke(v, RecyclerView.NO_POSITION, column)
                else -> excelClickListener?.invoke(v, RecyclerView.NO_POSITION, RecyclerView.NO_POSITION)
              }
            }
          }

          override fun onDoubleTap(e: MotionEvent?): Boolean {
            return true
          }

          override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
            return true
          }
        })
      }
      detector?.onTouchEvent(e)
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) = Unit.apply { detector?.onTouchEvent(e) }
  }

  enum class ViewType {
    START_TOP, END_TOP, START_BOTTOM, END_BOTTOM
  }

  companion object {
    private const val ITEM_TYPE_TOP = 1
    private const val ITEM_TYPE_BOTTOM = 2
  }
}
