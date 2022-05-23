package com.aloe.rn

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ReactLayout(backCallback: (() -> Unit)? = null) {
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = "React Native", modifier = Modifier.padding(0.dp), fontSize = 16.sp)
        }, modifier = Modifier.height(40.dp), navigationIcon = {
            IconButton(onClick = {
                backCallback?.invoke()
                                 }, modifier = Modifier.padding(0.dp)) {
                Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "", tint = Color.White)
            }
        })
    }, modifier = Modifier.background(Color.Red)) { padding ->
        AndroidView(factory = { ReactView(it) }, modifier = Modifier.padding(padding)) {
            it.setBackBtnHandler { backCallback?.invoke() }.loadPage(null, Uri.parse("assets://index.android.bundle"))
        }
    }
}
