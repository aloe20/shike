package com.aloe.zxing

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ScanLayout(result: (String) -> Unit) {
    AndroidView(factory = { PreviewView(it) }, modifier = Modifier.fillMaxSize()) {
        it.post {
            it.bindCamera { content ->
                result.invoke(content)
            }
        }
    }
}
