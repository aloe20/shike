package com.aloe.shike.vm

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.aloe.bean.BannerBean
import kotlinx.coroutines.launch

class RecommendVm(app: Application) : BaseVm<RecommendState>(app) {
  var isBannerLoaded = false
    private set

  override fun loadData(type: Int) {
    isBannerLoaded = true
    viewModelScope.launch {
      liveData.postValue(RecommendState(repo.loadBanner().getOrNull()))
    }
  }
}

data class RecommendState(val banner: List<BannerBean>? = null)
