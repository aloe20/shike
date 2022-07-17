package com.aloe.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import com.aloe.chart.render.KLineChartRenderer
import com.aloe.chart.render.KlineCandleStickChartRenderer
import com.aloe.chart.render.KlineYAxisRenderer
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.renderer.CandleStickChartRenderer
import com.github.mikephil.charting.renderer.CombinedChartRenderer
import com.github.mikephil.charting.renderer.LineChartRenderer
import kotlin.math.max
import kotlin.math.min

class KlineCombinedChart : CombinedChart {
  private lateinit var lineChartRenderer: KLineChartRenderer
  private lateinit var candleStickChartRenderer: KlineCandleStickChartRenderer
  private val offset = 0.2F
  private val showCount = 45
  var highlightShow: Boolean = false
    private set

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

  override fun init() {
    super.init()
    lineChartRenderer = KLineChartRenderer(this, mAnimator, mViewPortHandler)
    candleStickChartRenderer = KlineCandleStickChartRenderer(this, mAnimator, mViewPortHandler)
    mAxisRendererLeft = KlineYAxisRenderer(mViewPortHandler, mAxisLeft, mLeftAxisTransformer)
    mChartTouchListener = KlineChartTouchListener(this, mViewPortHandler.matrixTouch, 3f)
  }

  override fun setData(data: CombinedData?) {
    super.setData(data)
    data?.also { combinedData ->
      (mRenderer as CombinedChartRenderer).subRenderers.also { list ->
        list.forEachIndexed { index, dataRenderer ->
          when (dataRenderer) {
            is LineChartRenderer -> list[index] = lineChartRenderer
            is CandleStickChartRenderer -> list[index] = candleStickChartRenderer
          }
        }
      }
      var entryCount: Int = Int.MIN_VALUE
      combinedData.dataSets.forEach {
        entryCount = max(entryCount, it.entryCount)
      }
      xAxis.axisMinimum = -0.5F
      xAxis.axisMaximum = entryCount - 0.5F
      zoom(1F * entryCount / showCount, 1F, width.toFloat(), height / 2F)
      updateRange(entryCount - showCount, entryCount - 1)
    }
  }

  override fun highlightValue(high: Highlight?, callListener: Boolean) {
    isDragEnabled = high == null
    highlightShow = !isDragEnabled
    super.highlightValue(high, callListener)
  }

  override fun onDraw(canvas: Canvas) {
    val tmp = mIndicesToHighlight
    mIndicesToHighlight = null
    super.onDraw(canvas)
    mIndicesToHighlight = tmp
    if (valuesToHighlight()) {
      mRenderer.drawHighlighted(canvas, mIndicesToHighlight)
    }
  }

  fun initChart() {
    isScaleXEnabled = true
    isScaleYEnabled = false
    legend.isEnabled = false
    xAxis.isEnabled = false
    axisRight.isEnabled = false
    description.isEnabled = false
    isDoubleTapToZoomEnabled = false
    isHighlightPerTapEnabled = false
    isHighlightPerDragEnabled = true
    setPinchZoom(false)
    setMaxVisibleValueCount(0)
    setViewPortOffsets(0F, 0F, 0F, 0F)
    setBorderWidth(0.6F)
    setDrawBorders(true)
    setDrawBarShadow(false)
    setBorderColor(Color.parseColor("#FFCCCCCC"))
    onChartGestureListener = ChartGestureListener(this)
    with(xAxis) {
      setDrawLabels(false)
      setDrawGridLines(false)
      axisMinimum = -0.5F
    }
    with(axisLeft) {
      setLabelCount(5, true)
      setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
      yOffset = 0F
      xOffset = 0F
      setDrawLabels(true)
      setDrawGridLines(false)
      valueFormatter = DefaultValueFormatter(2)
    }
  }

  fun getRange() = candleStickChartRenderer.getRange()

  fun updateRange(start: Int, end: Int) {
    var min = Float.MAX_VALUE
    var max = Float.MIN_VALUE
    combinedData?.dataSets?.forEach {
      for (index in start..end) {
        it.getEntryForIndex(index).also { entry ->
          when (entry) {
            is CandleEntry -> {
              min = min(min, entry.low)
              max = max(max, entry.high)
            }
            else -> {
              if (!entry.y.isNaN()) {
                min = min(min, entry.y)
                max = max(max, entry.y)
              }
            }
          }
        }
      }
    }
    val axis = (max - min) * offset
    axisLeft.axisMinimum = min - axis
    axisLeft.axisMaximum = max + axis
    notifyDataSetChanged()
    postInvalidate()
  }
}
