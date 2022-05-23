package com.aloe.flu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.flutter.embedding.android.FlutterView

val paddingStatus = mutableStateOf(false)

@Composable
fun FlutterLayout() {
    val engine = FlutterViewEngine(LocalContext.current.applicationContext)
    paddingStatus.value = true
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        AndroidView(factory = { FlutterView(it) }, Modifier.fillMaxSize()) {
            engine.attachFlutterView(it)
        }
    }
}
