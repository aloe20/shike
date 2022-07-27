package com.aloe.http

import com.aloe.bean.BannerBean
import kotlinx.coroutines.flow.Flow

interface IHttp {
  suspend fun loadBanner(): Result<List<BannerBean>?>
  fun download(url: String, path:String?=null):Flow<Int>
}
