package com.aloe.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.aloe.proto.BannerList
import java.io.InputStream
import java.io.OutputStream

internal object BannerSerializer : Serializer<BannerList?> {
    override val defaultValue: BannerList = BannerList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): BannerList? = runCatching { BannerList.parseFrom(input) }.getOrNull()

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: BannerList?, output: OutputStream) {
        t?.writeTo(output)
    }
}

internal val Context.bannerDataStore: DataStore<BannerList?> by dataStore("banner.pb", BannerSerializer)
