package com.aloe.chart.index

import com.aloe.chart.KlineEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet

interface IndexLine {
  fun initCycleAndColor()
  fun getLineDataSet(entries: List<KlineEntry>): List<LineDataSet>
  fun getEntry(cycle: Int, entries: List<KlineEntry>): List<Entry>
  fun calcIndex(cycle: Int, entries: List<KlineEntry>): List<Float>
}
