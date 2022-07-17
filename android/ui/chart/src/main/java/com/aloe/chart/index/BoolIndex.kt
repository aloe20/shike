package com.aloe.chart.index

import android.graphics.Color
import com.aloe.chart.KlineEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 布林线指标，由中轨线(N日移动平均线MA)，上轨线(中轨线+K倍的标准差)和下轨线(中轨线-K倍的标准差)组成.
 * MA(n)=SUM(c)/n
 * MD=SQRT(SUM((c-ma)(c-ma), n-1)/n)
 * MB=MA(n-1)
 * UP=MB+k*MD
 * DN=MB-k*MD
 *
 * n 周期，一般设置为N1=5，N2=10，N3=20，N4=60，N5=120，N6=250
 * k 标准差倍数，一般设为2
 * BOLL指标三条线，第一个为上轨线，第二个为中轨线，第三个为下轨线
 */
class BoolIndex : MaIndex() {
  private val k = 2F
  override fun initCycleAndColor() {
    args.add(Triple(5, Color.RED, "UPPER"))
    args.add(Triple(5, Color.BLUE, "MEDIUM"))
    args.add(Triple(5, Color.BLACK, "DOWN"))
  }

  override fun getLineDataSet(entries: List<KlineEntry>): List<LineDataSet> {
    var mdSum = 0F
    val cycle = args[0].first
    val ma = getEntry(cycle, entries)
    return getEntry(cycle, entries).foldIndexed(
      mutableListOf<MutableList<Entry>>(
        mutableListOf(),
        mutableListOf(),
        mutableListOf()
      )
    ) { index, acc, _ ->
      val x = index.toFloat()
      if (index < cycle) {
        acc[0].add(Entry(x, Float.NaN))
        acc[1].add(Entry(x, Float.NaN))
        acc[2].add(Entry(x, Float.NaN))
      } else {
        mdSum += (entries[index - 1].close - ma[index - 1].y).pow(2)
        if (!ma[index - cycle].y.isNaN()) {
          mdSum -= (entries[index - cycle].close - ma[index - cycle].y).pow(2)
        }
        val mdValue = sqrt(mdSum / cycle)
        val mbValue = ma[index - 1].y
        acc[0].add(Entry(x, mbValue + k * mdValue))
        acc[1].add(Entry(x, mbValue))
        acc[2].add(Entry(x, mbValue - k * mdValue))
      }
      acc
    }.foldIndexed(mutableListOf()) { index, acc, mutableList ->
      acc.also {
        it.add(LineDataSet(mutableList, args[index].third).apply {
          setDrawCircles(false)
          isHighlightEnabled = false
          color = args[index].second
          mode = LineDataSet.Mode.CUBIC_BEZIER
        })
      }
    }
  }
}
