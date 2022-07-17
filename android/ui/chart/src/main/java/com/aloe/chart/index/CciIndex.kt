package com.aloe.chart.index

import android.graphics.Color
import com.aloe.chart.KlineEntry

class CciIndex : MaIndex() {

  override fun initCycleAndColor() {
    args.add(Triple(26, Color.RED, "CCI"))
  }

  override fun calcIndex(cycle: Int, entries: List<KlineEntry>): List<Float> {
    val ma = super.calcIndex(cycle, entries)
    var mdSum = 0F
    return ma.foldIndexed(mutableListOf()) { index, acc, value ->
      if (index < (cycle - 1)) {
        acc.add(Float.NaN)
      } else {
        mdSum += value - entries[index].close
        val maTmp = ma[index - cycle]
        if (!maTmp.isNaN()) {
          mdSum -= maTmp - entries[index - cycle].close
        }
        acc.add((entries[index].medium() - value) / (mdSum / cycle) / 0.015F)
      }
      acc
    }
  }
}
