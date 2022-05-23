package com.aloe.shike

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

open class BaseVm : ViewModel() {
    private val map: MutableMap<String, Job?> = mutableMapOf()
    protected fun compat(key: String, block: suspend () -> Unit) {
        map[key]?.cancel()
        map[key] = viewModelScope.launch {
            block.invoke()
        }
    }

    override fun onCleared() {
        super.onCleared()
        map.forEach { it.value?.cancel() }
    }
}
