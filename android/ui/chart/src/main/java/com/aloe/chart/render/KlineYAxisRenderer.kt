package com.aloe.chart.render

import android.graphics.Canvas
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.renderer.YAxisRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

class KlineYAxisRenderer(
  viewPortHandler: ViewPortHandler, yAxis: YAxis, trans: Transformer
) : YAxisRenderer(viewPortHandler, yAxis, trans) {
  private val labelVerticalOffset = Utils.convertDpToPixel(5F)

  override fun drawYLabels(c: Canvas, fixedPosition: Float, positions: FloatArray, offset: Float) {
    //super.drawYLabels(c, fixedPosition, positions, offset)
    val from = if (mYAxis.isDrawBottomYLabelEntryEnabled) 0 else 1
    val to = if (mYAxis.isDrawTopYLabelEntryEnabled) mYAxis.mEntryCount else mYAxis.mEntryCount - 1
    val medium = (from + to) / 2
    for (i in from until to) {
      val offset2 = when {
        i < medium -> offset - labelVerticalOffset + 1
        i > medium -> offset + labelVerticalOffset + 1
        else -> offset
      }
      val text = mYAxis.getFormattedLabel(i)
      c.drawText(text, fixedPosition + 1, positions[i * 2 + 1] + offset2, mAxisLabelPaint)
    }
  }
}
