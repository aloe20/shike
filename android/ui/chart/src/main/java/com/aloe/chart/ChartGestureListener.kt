package com.aloe.chart

import android.view.MotionEvent
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import java.lang.ref.WeakReference

class ChartGestureListener(chart: KlineCombinedChart) : OnChartGestureListener {
  private var startIndex = -1
  private var endIndex = -1
  private var isLongPress = false
  private val chart: WeakReference<KlineCombinedChart> = WeakReference(chart)

  override fun onChartGestureEnd(me: MotionEvent, cg: ChartTouchListener.ChartGesture) {
    isLongPress = false
    updateRange(true)
  }

  override fun onChartFling(me1: MotionEvent, me2: MotionEvent, velocityX: Float, velocityY: Float) {
    isLongPress = false
    updateRange(false)
  }

  override fun onChartSingleTapped(me: MotionEvent) {
    chart.get()?.highlightValue(null, true)
  }

  override fun onChartGestureStart(me: MotionEvent, cg: ChartTouchListener.ChartGesture) {
    isLongPress = true
    updateRange(false)
  }

  override fun onChartScale(me: MotionEvent, scaleX: Float, scaleY: Float) {
    updateRange(true)
  }

  override fun onChartLongPressed(me: MotionEvent) {
    chart.get()?.takeIf { isLongPress }?.apply {
      highlightValue(getHighlightByTouchPoint(me.x, me.y), true)
    }
  }

  override fun onChartDoubleTapped(me: MotionEvent) {

  }

  override fun onChartTranslate(me: MotionEvent, dX: Float, dY: Float) {
    updateRange(false)
  }

  private fun updateRange(forceUpdate: Boolean) {
    chart.get()?.apply {
      val range = getRange()
      if (forceUpdate || (startIndex != range[0] && endIndex != range[1])) {
        startIndex = range[0]
        endIndex = range[1]
        updateRange(startIndex, endIndex)
      }
    }
  }
}
