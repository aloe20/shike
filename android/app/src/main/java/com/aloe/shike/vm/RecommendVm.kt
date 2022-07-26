package com.aloe.shike.vm

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.aloe.bean.BannerBean
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
@HiltViewModel
class RecommendVm @Inject constructor(app: Application) : BaseVm<RecommendState>(app) {
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
