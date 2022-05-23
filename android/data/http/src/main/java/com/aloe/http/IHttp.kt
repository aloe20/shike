package com.aloe.http

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import com.aloe.bean.BannerBean

interface IHttp {
    suspend fun loadBanner(): Result<List<BannerBean>?>
    fun download(url: String): LiveData<WorkInfo>
}
