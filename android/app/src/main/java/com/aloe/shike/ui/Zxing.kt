package com.aloe.shike.ui

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.launch

private const val RATIO_4_3 = 4.0 / 3.0
private const val RATIO_16_9 = 16.0 / 9.0

@Composable
fun ZxingLayout(result: (String) -> Unit) {
  AndroidView(factory = { PreviewView(it) }, modifier = Modifier.fillMaxSize()) { view ->
    view.bindCamera {
      view.post { result.invoke(it) }
    }
  }
}

fun createQrCode(width: Int, height: Int, text: String, logo: Drawable? = null): Bitmap {
  val hints = mutableMapOf(
    EncodeHintType.CHARACTER_SET to "UTF-8",
    EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
//        EncodeHintType.MARGIN to 10
  )
  val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints)
  val colors = IntArray(width * height)
  for (i in 0 until width) {
    for (j in 0 until height) {
      colors[i * width + j] = if (matrix.get(i, j)) Color.BLACK else Color.WHITE
    }
  }
  val bitmap = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888).copy(Bitmap.Config.ARGB_8888, true)
  logo?.also {
    val bmp = Bitmap.createBitmap(it.intrinsicWidth, it.intrinsicHeight, Bitmap.Config.ARGB_8888)
      .copy(Bitmap.Config.ARGB_8888, true)
    var canvas = Canvas(bmp)
    it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
    it.draw(canvas)
    canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = Color.WHITE
    var logoWidth = bmp.width + 10
    var logoHeight = bmp.height + 10
    canvas.drawRect(
      (width - logoWidth) / 2F,
      (height - logoHeight) / 2F,
      (width + logoWidth) / 2F,
      (height + logoHeight) / 2F,
      paint
    )
    logoWidth = bmp.width
    logoHeight = bmp.height
    canvas.drawBitmap(bmp, (width - logoWidth) / 2F, (height - logoHeight) / 2F, paint)
  }
  return bitmap
}

fun decodeQrCode(context: Context, uri: Uri): String? {
  val options = BitmapFactory.Options()
  options.inJustDecodeBounds = true
  BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, options)
  options.inSampleSize = options.outHeight / 400
  options.inSampleSize = max(1, options.inSampleSize)
  options.inJustDecodeBounds = false
  return BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, options)?.let {
    val pixels = IntArray(it.width * it.height)
    it.getPixels(pixels, 0, it.width, 0, 0, it.width, it.height)
    val binaryBitmap = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(it.width, it.height, pixels)))
    runCatching {
      QRCodeReader().decode(
        binaryBitmap,
        mutableMapOf(DecodeHintType.CHARACTER_SET to "UTF-8")
      ).text
    }.getOrNull()
  }
}

private fun PreviewView.bindCamera(result: (String) -> Unit) {
  val owner = ViewTreeLifecycleOwner.get(this) ?: throw NullPointerException("owner is null")
  // 获取用于设置全屏分辨率相机的屏幕值
  val cameraExecutor = Executors.newSingleThreadExecutor()
  owner.lifecycleScope.launch {
    owner.repeatOnLifecycle(Lifecycle.State.DESTROYED) {
      cameraExecutor.shutdown()
    }
  }
  val metrics = context.resources.displayMetrics
  //获取使用的屏幕比例分辨率属性
  val previewRatio = max(metrics.widthPixels / 2, metrics.heightPixels / 2).toDouble() / min(
    metrics.widthPixels / 2,
    metrics.heightPixels / 2
  )
  val screenAspectRatio =
    if (abs(previewRatio - RATIO_4_3) <= abs(previewRatio - RATIO_16_9)) AspectRatio.RATIO_4_3 else AspectRatio.RATIO_16_9
  val width = measuredWidth
  val height = if (screenAspectRatio == AspectRatio.RATIO_16_9) width * RATIO_16_9 else width * RATIO_4_3
  val size = Size(width, height.toInt())
  //获取旋转角度
  //和Fragment的生命周期绑定
  val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()//设置所选相机
  val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
  post {
    val rotation = display.rotation
    cameraProviderFuture.addListener({
      val cameraProvider = cameraProviderFuture.get()
      // 预览用例
      val preview = Preview.Builder().setTargetResolution(size).setTargetRotation(rotation).build()
      // 必须在重新绑定用例之前取消之前绑定
      cameraProvider.unbindAll()
      runCatching {
        //获取相机实例
        cameraProvider.bindToLifecycle(owner, cameraSelector, preview,
          ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetResolution(size).setTargetRotation(rotation).build(),
          ImageAnalysis.Builder().setTargetResolution(size).setTargetRotation(rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).setImageQueueDepth(4).build()
            .apply {
              setAnalyzer(cameraExecutor, Zxing(result))
            })
        //设置预览的view
        preview.setSurfaceProvider(surfaceProvider)
      }
    }, ContextCompat.getMainExecutor(context))
  }
}

class Zxing(private val block: (String) -> Unit) : ImageAnalysis.Analyzer {
  private var text: String = ""
  private val reader: MultiFormatReader = MultiFormatReader().apply {
    setHints(
      mutableMapOf(
        DecodeHintType.CHARACTER_SET to "UTF-8",
        DecodeHintType.POSSIBLE_FORMATS to mutableSetOf(
          BarcodeFormat.UPC_A,
          BarcodeFormat.UPC_E,
          BarcodeFormat.EAN_13,
          BarcodeFormat.EAN_8,
          BarcodeFormat.RSS_14,
          BarcodeFormat.RSS_EXPANDED,
          BarcodeFormat.CODE_39,
          BarcodeFormat.CODE_93,
          BarcodeFormat.CODE_128,
          BarcodeFormat.ITF,
          BarcodeFormat.CODABAR,
          BarcodeFormat.DATA_MATRIX,
          BarcodeFormat.QR_CODE
        )
      )
    )
  }

  /**
   * 将buffer写入数组
   */
  private fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    val data = ByteArray(remaining())
    get(data)
    return data
  }

  override fun analyze(image: ImageProxy) {
    //如果不是yuv_420_888格式直接不处理
    if (ImageFormat.YUV_420_888 != image.format) {
      image.close()
      return
    }
    //将buffer数据写入数组
    val data = image.planes[0].buffer.toByteArray()
    //获取图片宽高
    val height = image.height
    val width = image.width
    //将图片旋转，这是竖屏扫描的关键一步，因为默认输出图像是横的，我们需要将其旋转90度
    val rotationData = ByteArray(data.size)
    var j: Int
    var k: Int
    for (y in 0 until height) {
      for (x in 0 until width) {
        j = x * height + height - y - 1
        k = x + y * width
        rotationData[j] = data[k]
      }
    }
    //zxing核心解码块，因为图片旋转了90度，所以宽高互换，最后一个参数是左右翻转
    val source = PlanarYUVLuminanceSource(rotationData, height, width, 0, 0, height, width, false)
    val bitmap = BinaryBitmap(HybridBinarizer(source))
    image.close()
    runCatching { reader.decode(bitmap) }.getOrNull()?.text?.takeUnless {
      it == text
    }?.also {
      text = it
      block.invoke(it)
    }
  }
}
