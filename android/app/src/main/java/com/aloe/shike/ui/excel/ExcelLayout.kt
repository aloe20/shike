package com.aloe.shike.ui.excel

import android.graphics.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.aloe.shike.R

@Composable
fun ExcelLayout(click: (() -> Unit)? = null) {
    val viewModel = hiltViewModel<ExcelVm>()
    val uiState by viewModel.getUiState().collectAsState()
    viewModel.loadData()
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = "列表", modifier = Modifier.padding(0.dp), fontSize = 16.sp)
        }, modifier = Modifier.height(40.dp), navigationIcon = {
            IconButton(onClick = { click?.invoke() }, modifier = Modifier.padding(0.dp)) {
                Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "", tint = Color.White)
            }
        })
    }, modifier = Modifier.background(Color.Red)) { padding ->
        AndroidView(factory = { RecyclerView(it) }, modifier = Modifier.padding(padding)) {
            (it.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            val adapter = with(it.resources){
                val left = getDimensionPixelSize(R.dimen.excel_title_width)
                val top = getDimensionPixelSize(R.dimen.excel_title_height)
                val right = left + getDimensionPixelSize(R.dimen.cell_width)
                val bottom = top + getDimensionPixelSize(R.dimen.cell_height)
                MyAdapter(Rect(left, top, right, bottom))
            }
            it.adapter = adapter
            it.addItemDecoration(DividerItemDecoration(it.context, DividerItemDecoration.VERTICAL))
            uiState.quoteBean?.also { list->
                with(adapter){
                    setTopData(getTopData())
                    addData(list, true)
                    updateSort(QuoteTop.LAST_PRICE, true)
                }
            }
        }
    }
}

private fun getTopData() = mutableListOf(
    QuoteTop.LAST_PRICE, QuoteTop.UP_DOWN, QuoteTop.TURNOVER_RATE,
    QuoteTop.VOLUME, QuoteTop.AMOUNT, QuoteTop.RATIO, QuoteTop.SA, QuoteTop.UP_DOWN_SPEED,
    QuoteTop.PE_RATIO, QuoteTop.TOT_VAL, QuoteTop.CIR_VAL
)
