package com.aloe.http

import com.aloe.bean.ArticleBean
import com.aloe.bean.BannerBean
import com.aloe.bean.HttpBean
import kotlinx.coroutines.flow.Flow

interface IHttp {
  suspend fun loadBanner(): Result<List<BannerBean>?>
  suspend fun loadTop(): Result<List<ArticleBean>?>
  fun download(url: String, path:String?=null):Flow<Int>
}
