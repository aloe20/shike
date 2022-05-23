package com.aloe.shike.ui.main

import androidx.lifecycle.viewModelScope
import com.aloe.bean.BannerBean
import com.aloe.shike.BaseVm
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class MainVm @Inject constructor(private val model: MainModel) : BaseVm() {
  private val _uiState = MutableStateFlow(MainUiState())
  fun loadUiData() {
    compat("uiState") {
      combine(model.loadPrivacy(), flowOf(model.loadBanner())) { f1, f2 ->
        MainUiState(f1, f2)
      }.collect {
        _uiState.value = it
      }
    }
  }

  fun getUiState(): StateFlow<MainUiState> = _uiState.asStateFlow()

  fun hidePrivacy() {
    compat("privacy") { model.hidePrivacy() }
  }

  fun initSocket() {
    viewModelScope.launch {
      model.initSocket()
    }
  }
}

data class MainUiState(val privacyVisible: Boolean = false, val banners: List<BannerBean> = listOf())
