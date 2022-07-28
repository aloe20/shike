package com.aloe.http

import android.content.Context
import com.aloe.bean.moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.*
import javax.inject.Singleton
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
internal class HttpModule {
  @Provides
  @Singleton
  fun getExecutor(): ExecutorService = ThreadPoolExecutor(
    5, 10, 60, TimeUnit.SECONDS,
    ArrayBlockingQueue(10)
  )

  @Provides
  @Singleton
  fun getOkHttpClient(executor: ExecutorService): OkHttpClient = OkHttpClient.Builder().dispatcher(Dispatcher(executor))
    .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build()

  @Provides
  @Singleton
  fun getHttp(@ApplicationContext ctx: Context, client: OkHttpClient): IHttp {
    return HttpImpl(
      ctx, Retrofit.Builder()
        .baseUrl(BuildConfig.host).addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
        .client(client).build().create()
    )
  }
}
