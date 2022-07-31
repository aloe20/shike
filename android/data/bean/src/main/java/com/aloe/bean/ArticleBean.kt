package com.aloe.bean

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ArticleBean(
  val title: String? = null,
  val author: String? = null,
  val superChapterName: String? = null,
  val niceDate: String? = null,
  val link: String? = null
)
