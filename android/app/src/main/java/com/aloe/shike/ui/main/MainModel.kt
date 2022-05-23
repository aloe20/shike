package com.aloe.shike.ui.main

import com.aloe.bean.BannerBean
import com.aloe.shike.Repository
import com.aloe.shike.ktx.log
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class MainModel @Inject constructor(private val repo: Repository) {
    suspend fun loadBanner(): List<BannerBean> = repo.loadBanner().getOrNull() ?: listOf()

    suspend fun initSocket() {
        repo.initSocket()
        delay(3000)
        repo.startUdp(3000)
        repo.getUdpReceive().collect {
            String(it.sliceArray(6 until it.size)).log()
        }
    }

    suspend fun loadPrivacy(): Flow<Boolean> = repo.getPrivacyVisible()

    suspend fun hidePrivacy() {
        repo.putPrivacyVisible(false)
    }
}
