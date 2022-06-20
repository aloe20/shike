@file:Suppress("BlockingMethodInNonBlockingContext")

package com.aloe.shike.ui.excel

import com.aloe.bean.QuoteBean
import com.aloe.shike.BaseVm
import com.aloe.shike.Repository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ExcelVm @Inject constructor(
    private val repo: Repository,
    private val moshi: Moshi
) : BaseVm() {
    private val _uiState = MutableStateFlow(ExcelUiState())
    fun loadData() {
        compat("excel") {
            repo.getAssetsStr("ruotes.json")?.also {
                _uiState.value = ExcelUiState(
                    moshi.adapter<List<QuoteBean>>(Types.newParameterizedType(List::class.java, QuoteBean::class.java))
                        .lenient().fromJson(it)
                )
            }
        }
    }

    fun getUiState(): StateFlow<ExcelUiState> = _uiState.asStateFlow()
}

data class ExcelUiState(val quoteBean: List<QuoteBean>? = listOf())
