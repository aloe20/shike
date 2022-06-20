package com.aloe.zxing

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class CodeAnalyzer(private val result: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val reader: MultiFormatReader = MultiFormatReader().apply { setHints(hints) }
    private var text:String = ""

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
        image.use {
            val scanResult = reader.decode(bitmap)
            if (text != scanResult.text) {
                text = scanResult.text
                result.invoke(scanResult.text)
            }
        }
    }
}
