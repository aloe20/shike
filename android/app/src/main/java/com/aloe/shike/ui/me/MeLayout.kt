package com.aloe.shike.ui.me

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.aloe.shike.R
import com.aloe.shike.app.showToast
import com.aloe.shike.ktx.log
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
    val imgUri =
        remember { mutableStateOf(Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/drawable/ic_person")) }
    val drawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val registry = LocalActivityResultRegistryOwner.current?.activityResultRegistry
    val launcher = registry?.register(
        "camera", ActivityResultContracts.TakePicture()
    ) {
        scope.launch { drawerState.close() }
        if (it) {
            imgUri.value = cameraUri
            saveImg(context, cameraUri)
        }
    }
    val launcher2 = registry?.register("album", ActivityResultContracts.StartActivityForResult()) {
        scope.launch { drawerState.close() }
        it.data?.data?.also { uri ->
            imgUri.value = uri
            val code = decodeQrCode(context, uri)
            context.showToast(code ?: "没有识别到二维码")
        }
    }
    BottomDrawer(drawerContent = {
        Text(
            text = "拍照", modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    launcher?.launch(cameraUri)
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
                painter = rememberAsyncImagePainter(model = imgUri.value),
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
                painter = BitmapPainter(image = createQrCode(800, 800, "aaaaaaaaaaaaaa", logo).asImageBitmap()),
                contentDescription = ""
            )
        }
    }
}

fun saveImg(context: Context, uri: Uri) {
    val resolver = context.contentResolver
    val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
    val image = MediaStore.Images.Media.insertImage(resolver, bitmap, "head", "aaa")
    image.log()
}
