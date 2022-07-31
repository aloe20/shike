package com.aloe.shike.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aloe.shike.generic.App

abstract class BaseVm<T>(app: Application) : AndroidViewModel(app) {
  private var isLoadedData = false
  protected val liveData = MutableLiveData<T>()
  protected val repo = (app as App).repository
  fun firstLoadData(){
    if (!isLoadedData) {
      isLoadedData = true
      loadData()
    }
  }
  abstract fun loadData()
  open fun getUiState():LiveData<T> = liveData
}
