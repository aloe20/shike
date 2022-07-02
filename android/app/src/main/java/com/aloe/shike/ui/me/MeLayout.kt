package com.aloe.shike.ui.me

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.aloe.shike.BuildConfig
import com.aloe.shike.app.AppObserver
import com.aloe.shike.app.showToast
import com.aloe.zxing.createQrCode
import com.aloe.zxing.decodeQrCode
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MeLayout() {
    val context = LocalContext.current
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "head.jpg")
    val cameraUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
    var imgUri by remember { mutableStateOf(Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/drawable/ic_person")) }
    val drawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val registry = LocalActivityResultRegistryOwner.current?.activityResultRegistry
    var isValid by remember { mutableStateOf(false) }
    val launcher = registry?.register(
        "camera1", ActivityResultContracts.TakePicture()
    ) {
        scope.launch { drawerState.close() }
        if (it) {
            imgUri = AppObserver.saveImg(context, cameraUri)
        }
    }
    val launcher2 = registry?.register("album", ActivityResultContracts.StartActivityForResult()) {
        scope.launch { drawerState.close() }
        it.data?.data?.also { uri ->
            imgUri = uri
            val code = decodeQrCode(context, uri)
            context.showToast(code ?: "没有识别到二维码")
        }
    }
    val launcher3: ActivityResultLauncher<String>? = LocalActivityResultRegistryOwner.current?.activityResultRegistry?.register(
        "me_permission",
        ActivityResultContracts.RequestPermission()
    ) { hasPermission ->
        if (isValid) {
            if (hasPermission) {
                launcher?.launch(cameraUri)
            } else {
                context.showToast("没有相机权限")
            }
            isValid = false
        }
    }
    BottomDrawer(drawerContent = {
        Text(
            text = "拍照", modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isValid = true
                    launcher3?.launch(Manifest.permission.CAMERA)
                }
                .padding(vertical = 8.dp), textAlign = TextAlign.Center
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFEEEEEE))
        )
        Text(
            text = "相册", modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    launcher2?.launch(Intent(Intent.ACTION_PICK).setType("image/*"))
                }
                .padding(vertical = 8.dp), textAlign = TextAlign.Center
        )
    }, drawerState = drawerState) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Image(
                painter = rememberAsyncImagePainter(model = imgUri),
                contentDescription = "",
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp)
                    .clip(CircleShape)
                    .clickable {
                        scope.launch { drawerState.open() }
                    }
            )

            val logo = ContextCompat.getDrawable(context, context.applicationInfo.icon)
            Image(
                painter = BitmapPainter(image = createQrCode(800, 800, "picture", logo).asImageBitmap()),
                contentDescription = ""
            )
        }
    }
}
