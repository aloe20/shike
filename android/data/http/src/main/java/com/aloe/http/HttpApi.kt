package com.aloe.http

import com.aloe.bean.BannerBean
import com.aloe.bean.HttpBean
import okhttp3.ResponseBody
import retrofit2.http.*

internal interface HttpApi {
  @GET("banner/json")
  suspend fun loadBanner(): HttpBean<List<BannerBean>>

  @GET
  @Streaming
  @Headers("noLog:true")
  suspend fun download(@Url url: String, @Header("RANGE") range: String): ResponseBody
}
