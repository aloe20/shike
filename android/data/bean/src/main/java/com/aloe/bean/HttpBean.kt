package com.aloe.bean

import com.squareup.moshi.JsonClass

/**
 * Http数据包装类型，该对象包含错误码[errorCode]，0代表有数据，错误描述[errorMsg]和业务数据[data]。
 */
@JsonClass(generateAdapter = true)
data class HttpBean<T>(val errorCode: Int? = 0, val errorMsg: String? = "", val data: T? = null)
