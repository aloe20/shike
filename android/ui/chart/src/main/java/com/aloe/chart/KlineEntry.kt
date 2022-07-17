package com.aloe.chart

import android.graphics.drawable.Drawable
import com.github.mikephil.charting.data.CandleEntry

class KlineEntry(
  x: Float,
  shadowH: Float,
  shadowL: Float,
  open: Float,
  close: Float,
  icon: Drawable? = null,
  data: Any? = null
) : CandleEntry(x, shadowH, shadowL, open, close, icon, data) {
  fun medium() = if (high.isNaN() || low.isNaN() || close.isNaN()) Float.NaN else (high + low + close) / 3F
}
