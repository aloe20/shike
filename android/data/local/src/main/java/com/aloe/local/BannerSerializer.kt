package com.aloe.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.aloe.proto.BannerList
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal object BannerSerializer : Serializer<BannerList?> {
  override val defaultValue: BannerList = BannerList.getDefaultInstance()

  override suspend fun readFrom(input: InputStream): BannerList? =
    runCatching { BannerList.parseFrom(input) }.getOrNull()

  override suspend fun writeTo(t: BannerList?, output: OutputStream) {
    withContext(Dispatchers.IO) {
      launch(Dispatchers.IO) {
        t?.writeTo(output)
      }
    }
  }
}

internal val Context.bannerDataStore: DataStore<BannerList?> by dataStore("banner.pb", BannerSerializer)
