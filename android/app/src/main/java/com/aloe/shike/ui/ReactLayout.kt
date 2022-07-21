package com.aloe.shike.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    Box(modifier = Modifier.height(40.dp)) {
      SmallTopAppBar(title = {
        Text(text = "React Native", modifier = Modifier.padding(0.dp), fontSize = 16.sp)
      }, navigationIcon = {
        IconButton(onClick = { backCallback?.invoke() }, modifier = Modifier.padding(0.dp)) {
          Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "")
        }
      })
      Spacer(
        modifier = Modifier
          .fillMaxWidth()
          .height(0.3.dp)
          .background(Color.Gray)
          .align(Alignment.BottomCenter)
      )
    }
  }) { padding ->
    AndroidView(
      factory = { ReactView(it).apply { setBackBtnHandler { backCallback?.invoke() } } },
      modifier = Modifier.padding(padding)
    ) {
      it.loadPage(Uri.parse("assets://index.bundle"))
    }
  }
}
