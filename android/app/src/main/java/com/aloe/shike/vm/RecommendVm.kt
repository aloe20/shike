package com.aloe.shike.vm

import android.app.Application
import com.aloe.bean.BannerBean

class RecommendVm(app: Application) : BaseVm<RecommendState>(app) {
  override suspend fun loadData(type: Int) {
    liveData.postValue(RecommendState(repo.loadBanner().getOrNull()))
  }
}

data class RecommendState(val banner: List<BannerBean>? = null)
