package com.aloe.chart

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
  private val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.CHINESE)
  private val date = Date()

  fun formatDate(time: Long, pattern: String = "yyyy/MM/dd"): String {
    simpleDateFormat.applyPattern(pattern)
    date.time = time
    return simpleDateFormat.format(date)
  }
}
