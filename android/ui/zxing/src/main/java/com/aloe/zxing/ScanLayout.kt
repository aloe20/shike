package com.aloe.zxing

import android.Manifest
import android.annotation.SuppressLint
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanLayout(result: (String) -> Unit) {
    val permission = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val status = remember { mutableStateOf(true) }
    when (permission.status) {
        PermissionStatus.Granted -> Box {
            if (status.value) {
                AndroidView(factory = { PreviewView(it) }, modifier = Modifier.fillMaxSize()) {
                    if (status.value) {
                        it.post {
                            it.bindCamera { content ->
                                status.value = false
                                result.invoke(content)
                            }
                        }
                    } else {
                        result.invoke("")
                    }
                }
            }
        }
        is PermissionStatus.Denied -> {
            var showState by remember { mutableStateOf(true) }
            if (showState) {
                val textToShow =
                    if (permission.status.shouldShowRationale) "相机对于这个应用程序很重要，请授予权限" else "需要相机权限才能使用此功能，请授予权限"
                AlertDialog(
                    onDismissRequest = { },
                    confirmButton = {
                        TextButton(onClick = {
                            showState = false
                            permission.launchPermissionRequest()
                        }) { Text(text = "同意") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showState = false
                            result.invoke("")
                        }) { Text(text = "不同意") }
                    },
                    title = { Text(text = "申请权限") },
                    text = { Text(text = textToShow) }
                )
            }
        }
    }
}
