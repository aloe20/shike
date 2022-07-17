package com.aloe.chart.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import com.aloe.chart.DateUtil
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.dataprovider.CandleDataProvider
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineScatterCandleRadarDataSet
import com.github.mikephil.charting.renderer.CandleStickChartRenderer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.max
import kotlin.math.min

class KlineCandleStickChartRenderer(
  chart: CandleDataProvider,
  animator: ChartAnimator,
  viewPortHandler: ViewPortHandler
) : CandleStickChartRenderer(chart, animator, viewPortHandler) {
  private val mShadowBuffers = FloatArray(4)
  private val mRangeBuffers = FloatArray(2)
  private val mHighlightLinePath = Path()
  private var labelBgColor: Int = Color.argb(255, 204, 204, 204)

  override fun drawDataSet(c: Canvas, dataSet: ICandleDataSet) {
    super.drawDataSet(c, dataSet)
    drawHighAndLowLabel(c, dataSet)
  }

  override fun drawHighlighted(c: Canvas, indices: Array<out Highlight>) {
    //super.drawHighlighted(c, indices)
    indices.forEach { high ->
      mChart.candleData.getDataSetByIndex(high.dataSetIndex)?.takeIf { it.isHighlightEnabled }?.also { set ->
        set.getEntryForXValue(high.x, high.y).takeIf { isInBoundsX(it, set) }?.let {
          mChart.getTransformer(set.axisDependency).getPixelForValues(it.x, it.close * mAnimator.phaseY)
        }?.also {
          high.setDraw(it.x.toFloat(), it.y.toFloat())
          drawHighlightLines(c, high.drawX, high.drawY, set)
        }
      }
    }
  }

  override fun drawHighlightLines(c: Canvas, x: Float, y: Float, set: ILineScatterCandleRadarDataSet<*>) {
    //super.drawHighlightLines(c, x, y, set)
    with(mHighlightPaint) {
      color = set.highLightColor
      strokeWidth = set.highlightLineWidth
      setPathEffect(set.dashPathEffectHighlight)
    }
    mHighlightLinePath.apply {
      (set.getEntryForXValue(x, y) as? CandleEntry)?.also { entry ->
        takeIf { set.isVerticalHighlightIndicatorEnabled }?.also {
          val date = (entry.data as Long)
            .let { DateUtil.formatDate(it * 1000) }
          val dateWidth = mValuePaint.measureText(date) * 1.2F
          val dateHeight = Utils.calcTextHeight(mValuePaint, date) * 1.4F
          reset()
          moveTo(x, mViewPortHandler.contentTop())
          lineTo(x, mViewPortHandler.contentBottom() - dateHeight)
          c.drawPath(this, mHighlightPaint)
          val color = mValuePaint.color
          mValuePaint.color = labelBgColor
          var left = x - dateWidth / 2
          var right = x + dateWidth / 2
          if (left < 0) {
            left = 0F
            right = dateWidth
          }
          if (right > c.width) {
            right = c.width.toFloat()
            left = right - dateWidth
          }
          val top = mViewPortHandler.contentBottom() - dateHeight
          c.drawRect(left, top, right, mViewPortHandler.contentBottom(), mValuePaint)
          mValuePaint.color = color
          val textX = min(max(x, dateWidth / 2), c.width - dateWidth / 2)
          c.drawText(date, textX, mViewPortHandler.contentBottom() - dateHeight * 0.2F, mValuePaint)
        }
        takeIf { set.isHorizontalHighlightIndicatorEnabled }?.also {
          val text = String.format("%.2f", entry.close)
          val textWidth = mValuePaint.measureText(text) * 1.2F
          val textHeight = Utils.calcTextHeight(mValuePaint, text)
          val color = mValuePaint.color
          mValuePaint.color = labelBgColor
          reset()
          if (textWidth * 2 > x) {
            mHighlightLinePath.moveTo(mViewPortHandler.contentLeft(), y)
            mHighlightLinePath.lineTo(mViewPortHandler.contentRight() - textWidth, y)
            c.drawPath(it, mHighlightPaint)
            c.drawRect(
              mViewPortHandler.contentRight() - textWidth,
              y - textHeight * 0.7F,
              mViewPortHandler.contentRight(),
              y + textHeight * 0.7F,
              mValuePaint
            )
            mValuePaint.color = color
            val textX = mViewPortHandler.contentRight() - textWidth / 2
            c.drawText(text, textX, y + textHeight / 2, mValuePaint)
          } else {
            mHighlightLinePath.moveTo(mViewPortHandler.contentLeft() + textWidth, y)
            mHighlightLinePath.lineTo(mViewPortHandler.contentRight(), y)
            c.drawPath(it, mHighlightPaint)
            c.drawRect(
              mViewPortHandler.contentLeft(),
              y - textHeight * 0.7F,
              textWidth,
              y + textHeight * 0.7F,
              mValuePaint
            )
            mValuePaint.color = color
            val textX = mViewPortHandler.contentLeft() + textWidth / 2
            c.drawText(text, textX, y + textHeight / 2, mValuePaint)
          }
        }
      }
    }
  }

  /**
   * 绘制最高价，最低价.
   * @param c 画布
   * @param dataSet 数据源
   */
  private fun drawHighAndLowLabel(c: Canvas, dataSet: ICandleDataSet) {
    calcHighAndLow(c.width, dataSet)
    mValuePaint.color = Color.BLACK
    val halfHigh = Utils.calcTextHeight(mValuePaint, "8.88→") / 2F
    if (mShadowBuffers[0] * 2 > c.width) {
      val highTxt = String.format("%.2f→", mShadowBuffers[1])
      val x = mShadowBuffers[0] - mValuePaint.measureText(highTxt) / 2F
      c.drawText(highTxt, x, mShadowBuffers[1], mValuePaint)
    } else {
      val highTxt = String.format("←%.2f", mShadowBuffers[1])
      val x = mShadowBuffers[0] + mValuePaint.measureText(highTxt) / 2F
      c.drawText(highTxt, x, mShadowBuffers[1], mValuePaint)
    }
    if (mShadowBuffers[2] * 2 > c.width) {
      val highTxt = String.format("%.2f→", mShadowBuffers[3])
      val x = mShadowBuffers[2] - mValuePaint.measureText(highTxt) / 2F
      c.drawText(highTxt, x, mShadowBuffers[3] + halfHigh, mValuePaint)
    } else {
      val highTxt = String.format("←%.2f", mShadowBuffers[3])
      val x = mShadowBuffers[2] + mValuePaint.measureText(highTxt) / 2F
      c.drawText(highTxt, x, mShadowBuffers[3] + halfHigh, mValuePaint)
    }
  }

  /**
   * 计算最高价最低价坐标.
   * @param width 坐标在0~width之间
   * @param dataSet 数据源
   */
  private fun calcHighAndLow(width: Int, dataSet: ICandleDataSet) {
    val trans = mChart.getTransformer(dataSet.axisDependency)
    var maxHigh = Float.MIN_VALUE
    var minLow = Float.MAX_VALUE
    for (j in mXBounds.min..(mXBounds.range + mXBounds.min)) {
      val e = dataSet.getEntryForIndex(j)
      mRangeBuffers[0] = e.x
      trans.pointValuesToPixel(mRangeBuffers)
      if (mRangeBuffers[0] >= 0 && mRangeBuffers[0] <= width) {
        if (e.high > maxHigh) {
          mShadowBuffers[0] = e.x
          mShadowBuffers[1] = e.high
          maxHigh = e.high
        }
        if (e.low < minLow) {
          mShadowBuffers[2] = e.x
          mShadowBuffers[3] = e.low
          minLow = e.low
        }
      }
    }
    mShadowBuffers[1] = mShadowBuffers[1] * mAnimator.phaseY
    mShadowBuffers[3] = mShadowBuffers[3] * mAnimator.phaseY
    trans.pointValuesToPixel(mShadowBuffers)
  }

  fun getRange(): IntArray {
    mXBounds.set(mChart, mChart.candleData.dataSets[0])
    return intArrayOf(mXBounds.min, mXBounds.max)
  }
}
