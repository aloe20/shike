package com.aloe.chart.index

import android.graphics.Color
import com.aloe.chart.KlineEntry

/**
 * N日移动平均线指标，N日收市价总和/N.
 * MA(n)=SUM(c)/n
 *
 * n 周期，一般设置为N1=5，N2=10，N3=20，N4=60，N5=120，N6=250
 */
open class MaIndex : BaseIndexLine() {

  override fun initCycleAndColor() {
    args.add(Triple(5, Color.RED, "MA5"))
    args.add(Triple(10, Color.BLACK, "MA10"))
    args.add(Triple(20, Color.BLUE, "MA20"))
  }

  override fun calcIndex(cycle: Int, entries: List<KlineEntry>): List<Float> {
    var sum = 0F
    return entries.foldIndexed(mutableListOf()) { index, acc, entry ->
      sum += entry.close
      if (index < cycle - 1) {
        acc.add(Float.NaN)
      } else {
        if (index - cycle > -1) {
          sum -= entries[index - cycle].close
        }
        acc.add(sum / cycle)
      }
      acc
    }
  }
}
