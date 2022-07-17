@file:Suppress("LeakingThis")

package com.aloe.chart.index

import com.aloe.chart.KlineEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet

abstract class BaseIndexLine : IndexLine {
  protected val args: MutableList<Triple<Int, Int, String>> = mutableListOf()

  init {
    initCycleAndColor()
  }

  override fun getLineDataSet(entries: List<KlineEntry>): List<LineDataSet> {
    return args.fold(mutableListOf()) { acc, triple ->
      acc.apply {
        add(LineDataSet(getEntry(triple.first, entries), triple.third).apply {
          color = triple.second
          setDrawCircles(false)
          isHighlightEnabled = false
          mode = LineDataSet.Mode.CUBIC_BEZIER
        })
      }
    }
  }

  override fun getEntry(cycle: Int, entries: List<KlineEntry>): List<Entry> {
    return calcIndex(cycle, entries).mapIndexed { index, value ->
      Entry(index.toFloat(), value)
    }
  }
}
