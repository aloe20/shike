package com.aloe.bean

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuoteBean(
    val market: String = "",
    val code: String = "",
    val name: String = "",
    val upDown: Double = Double.NaN,
    val upDownSpeed: Double = Double.NaN,
    val volume: Int = Int.MIN_VALUE,
    val sa: Double = Double.NaN,
    val ratio: Double = Double.NaN,
    val totVal: Double = Double.NaN,
    val amount: Double = Double.NaN,
    val cirVal: Double = Double.NaN,
    val peRatio: Double = Double.NaN,
    val lastPrice: Double = Double.NaN,
    val preClosePrice: Double = Double.NaN,
    val turnoverRate: Double = Double.NaN,
    val flowMainOut: Double = Double.NaN,
    val flowMainIn: Double = Double.NaN,
    val flowMainNetIn: Double = Double.NaN
)
