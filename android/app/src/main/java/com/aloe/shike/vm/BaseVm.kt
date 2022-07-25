package com.aloe.shike.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aloe.shike.generic.App

abstract class BaseVm<T>(app: Application) : AndroidViewModel(app) {
  protected val liveData = MutableLiveData<T>()
  protected val repo = (app as App).repository
  abstract suspend fun loadData(type: Int = 0)
  open fun getUiState():LiveData<T> = liveData
}
