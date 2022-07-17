package com.aloe.bean

import com.squareup.moshi.JsonClass

/**
 * 定义首页Banner数据类型，该对象包含标题[title]，描述[desc]，封面图[imagePath]和跳转链接[url]。
 */
@JsonClass(generateAdapter = true)
data class BannerBean(
  val title: String? = null,
  val desc: String? = null,
  val imagePath: String? = null,
  val url: String? = null
)
