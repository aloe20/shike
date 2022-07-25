package com.aloe.http

import android.content.Context
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

open class HttpRepository(
  context: Context, val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build(),
  val moshi: Moshi = Moshi.Builder().build()
) : IHttp by HttpImpl(
  context, Retrofit.Builder()
    .baseUrl(BuildConfig.host).addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
    .client(okHttpClient).build().create()
)
