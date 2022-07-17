package com.aloe.shike.ui.rn

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.aloe.rn.ReactView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactLayout(backCallback: (() -> Unit)? = null) {
  Scaffold(topBar = {
    SmallTopAppBar(title = {
      Text(text = "React Native", modifier = Modifier.padding(0.dp), fontSize = 16.sp)
    }, navigationIcon = {
      IconButton(onClick = {
        backCallback?.invoke()
      }, modifier = Modifier.padding(0.dp)) {
        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "", tint = Color.White)
      }
    })
  }) { padding ->
    AndroidView(factory = { ReactView(it) }, modifier = Modifier.padding(padding)) {
      it.setBackBtnHandler { backCallback?.invoke() }.loadPage(null, Uri.parse("assets://index.android.bundle"))
    }
  }
}
