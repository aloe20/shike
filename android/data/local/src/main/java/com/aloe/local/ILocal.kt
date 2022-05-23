package com.aloe.local

import com.aloe.proto.Banner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow

interface ILocal {
    suspend fun getAssetsStr(name: String): String
    suspend fun putPrivacyVisible(isVisible: Boolean)
    suspend fun getPrivacyVisible(): Flow<Boolean>
    suspend fun putBanner(banner: List<Banner>)
    suspend fun getBanner(): Flow<MutableList<Banner>>
}

@Module
@InstallIn(SingletonComponent::class)
internal interface LocalModule {
    @Binds
    fun getLocal(local: LocalImpl): ILocal
}
