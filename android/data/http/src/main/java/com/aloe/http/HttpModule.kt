package com.aloe.http

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
internal class HttpModule {
  @Provides
  @Singleton
  fun getOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
    .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build()

  @Provides
  @Singleton
  fun getMoshi(): Moshi = Moshi.Builder().build()

  @Provides
  @Singleton
  fun getHttpApi(client: OkHttpClient, moshi: Moshi): HttpApi = Retrofit.Builder()
    .baseUrl(BuildConfig.host).addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
    .client(client).build().create()

  @Provides
  @Singleton
  fun getHttp(httpImpl: HttpImpl): IHttp = httpImpl
}
