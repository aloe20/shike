package com.aloe.shike.ui.rich

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.aloe.shike.ui.main.LocalNavController

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PdfContentView() {
    //https://www.kotlincn.net/docs/kotlin-docs.pdf
    val vm: RichVm = hiltViewModel()
    val nav = LocalNavController.current
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = "PDF", modifier = Modifier.padding(0.dp))
        }, navigationIcon = {
            IconButton(onClick = { nav.navigateUp() }, modifier = Modifier.padding(0.dp)) {
                Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "", tint = Color.White)
            }
        }, modifier = Modifier.height(40.dp))
    }, modifier = Modifier.background(Color.Red)) {
        val isShowProgress = remember { mutableStateOf(true) }
        val livedata = remember { vm.loadPdf("http://192.168.137.1:3000/docs/a.pdf") }
        val state = livedata.observeAsState()
        state.value?.also { info ->
            val progress = info.progress.getInt("progress", 0)
            if (isShowProgress.value) {
                CircularProgressIndicator(progress = progress / 100F)
            }
            info.outputData.getString("file")?.also { path ->
                isShowProgress.value = false
                val pagingItems = vm.getPdfPage(path).collectAsLazyPagingItems()
                LazyColumn(modifier = Modifier.padding(it)) {
                    items(pagingItems.itemCount) { index ->
                        pagingItems[index]?.also {
                            Image(
                                bitmap = it.asImageBitmap(), contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                contentScale = ContentScale.FillWidth
                            )
                        }
                    }
                }
            }
        }
    }
}
