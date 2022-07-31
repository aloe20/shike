package com.aloe.shike.vm

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.aloe.bean.ArticleBean
import com.aloe.bean.BannerBean
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@HiltViewModel
class RecommendVm @Inject constructor(app: Application) : BaseVm<RecommendState>(app) {

  override fun loadData() {
    viewModelScope.launch {
      val banner = async { repo.loadBanner().getOrNull() }
      val top = async { repo.loadTop().getOrNull() }
      liveData.postValue(RecommendState(banner.await(), top.await()))
    }
  }
}

data class RecommendState(val banner: List<BannerBean>? = null, val top: List<ArticleBean>? = null)
