package com.aloe.chart.render

import android.graphics.Paint
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.max

class KLineChartRenderer(
  chart: LineDataProvider,
  animator: ChartAnimator,
  viewPortHandler: ViewPortHandler
) : LineChartRenderer(chart, animator, viewPortHandler) {
  /*override fun drawDataSet(c: Canvas, dataSet: ILineDataSet) {
      prevDrawDataSet(c, dataSet)
      super.drawDataSet(c, dataSet)
      lastDrawDataSet(c, dataSet)
  }

  private fun prevDrawDataSet(c: Canvas, dataSet: ILineDataSet): Unit = Unit

  private fun lastDrawDataSet(c: Canvas, dataSet: ILineDataSet): Unit = Unit*/

  override fun drawCubicBezier(dataSet: ILineDataSet) {
    //super.drawCubicBezier(dataSet)
    val phaseY = mAnimator.phaseY
    val trans = mChart.getTransformer(dataSet.axisDependency)
    mXBounds.set(mChart, dataSet)
    val intensity = dataSet.cubicIntensity
    cubicPath.reset()
    if (mXBounds.range >= 1) {
      var prevDx: Float
      var prevDy: Float
      var curDx: Float
      var curDy: Float
      val firstIndex = mXBounds.min + 1
      var prevPrev: Entry
      var prev = dataSet.getEntryForIndex(max(firstIndex - 2, 0))
      var cur = dataSet.getEntryForIndex(max(firstIndex - 1, 0))
      var next = cur
      var nextIndex = -1
      var hasStartPoint = prev.y.isNaN().not()
      if (hasStartPoint) {
        cubicPath.moveTo(cur.x, cur.y * phaseY)
      }
      for (j in (mXBounds.min + 1)..(mXBounds.range + mXBounds.min)) {
        prevPrev = prev
        prev = cur
        cur = if (nextIndex == j) next else dataSet.getEntryForIndex(j)
        nextIndex = if (j + 1 < dataSet.entryCount) j + 1 else j
        next = dataSet.getEntryForIndex(nextIndex)
        if (!prevPrev.y.isNaN()) {
          prevDx = (cur.x - prevPrev.x) * intensity
          prevDy = (cur.y - prevPrev.y) * intensity
          curDx = (next.x - prev.x) * intensity
          curDy = (next.y - prev.y) * intensity
          if (hasStartPoint) {
            cubicPath.cubicTo(
              prev.x + prevDx,
              (prev.y + prevDy) * phaseY,
              cur.x - curDx,
              (cur.y - curDy) * phaseY,
              cur.x,
              cur.y * phaseY
            )
          } else {
            cubicPath.moveTo(prevPrev.x, prevPrev.y * phaseY)
            hasStartPoint = true
          }
        }
      }
    }
    if (dataSet.isDrawFilledEnabled) {
      cubicFillPath.reset()
      cubicFillPath.addPath(cubicPath)
      drawCubicFill(mBitmapCanvas, dataSet, cubicFillPath, trans, mXBounds)
    }
    mRenderPaint.color = dataSet.color
    mRenderPaint.style = Paint.Style.STROKE
    trans.pathValueToPixel(cubicPath)
    mBitmapCanvas.drawPath(cubicPath, mRenderPaint)
    mRenderPaint.pathEffect = null
  }

  override fun drawHorizontalBezier(dataSet: ILineDataSet) {
    //super.drawHorizontalBezier(dataSet)
    val phaseY = mAnimator.phaseY
    val trans = mChart.getTransformer(dataSet.axisDependency)
    mXBounds.set(mChart, dataSet)
    cubicPath.reset()
    if (mXBounds.range >= 1) {
      var prev = dataSet.getEntryForIndex(mXBounds.min)
      var cur = prev
      var hasStartPoint = prev.y.isNaN().not()
      if (hasStartPoint) {
        cubicPath.moveTo(cur.x, cur.y * phaseY)
      }
      for (j in (mXBounds.min + 1)..(mXBounds.range + mXBounds.min)) {
        prev = cur
        cur = dataSet.getEntryForIndex(j)
        if (!prev.y.isNaN()) {
          if (hasStartPoint) {
            val cpx = prev.x + (cur.x - prev.x) / 2F
            cubicPath.cubicTo(cpx, prev.y * phaseY, cpx, cur.y * phaseY, cur.x, cur.y * phaseY)
          } else {
            cubicPath.moveTo(prev.x, prev.y * phaseY)
            hasStartPoint = true
          }
        }
      }
    }
    if (dataSet.isDrawFilledEnabled) {
      cubicFillPath.reset()
      cubicFillPath.addPath(cubicPath)
      drawCubicFill(mBitmapCanvas, dataSet, cubicFillPath, trans, mXBounds)
    }
    mRenderPaint.color = dataSet.color
    mRenderPaint.style = Paint.Style.STROKE
    trans.pathValueToPixel(cubicPath)
    mBitmapCanvas.drawPath(cubicPath, mRenderPaint)
    mRenderPaint.pathEffect = null
  }
}
