package com.aloe.http

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import com.aloe.bean.BannerBean

internal class HttpImpl constructor(private val ctx: Context, private val api: HttpApi) : IHttp {
  override suspend fun loadBanner(): Result<List<BannerBean>?> = runCatching { api.loadBanner().data }
  override fun download(url: String): LiveData<WorkInfo> = DownloadWork.download(ctx, url)
}
