package com.aloe.shike.app

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.TextUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.contentValuesOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ActivityRecreator
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AppObserver(private val context: App, private val handler: Handler) : ContentObserver(handler) {
  private var insertUri: Uri? = null
  private val projection = arrayOf(MediaStore.Images.ImageColumns.DATA)
  override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
    //兼容华为手机父类没有此方法
    //super.onChange(selfChange, uri, flags)
    if (uri == null) {
      return
    }
    when (flags) {
      //兼容华为手机
      Process.ROOT_UID -> preHandlerUri(uri)
      ContentResolver.NOTIFY_INSERT -> insertUri = uri
      ContentResolver.NOTIFY_UPDATE -> {
        if (Objects.equals(insertUri, uri)) {
          preHandlerUri(uri)
        }
      }
    }
  }

  private fun preHandlerUri(uri: Uri) {
    val tmp = uri.toString().run { substring(lastIndexOf("/") + 1) }
    if (!TextUtils.isEmpty(tmp) && TextUtils.isDigitsOnly(tmp) && !ignoreUris.contains(uri.toString())) {
      if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
        handleUri(uri)
      } else {
        (ActivityRecreator.getTopActivity() as? FragmentActivity)?.also { activity ->
          activity.runOnUiThread {
            activity.supportFragmentManager.beginTransaction()
              .replace(android.R.id.content, PermissionFragment().setCallback {
                if (it) {
                  handleUri(uri)
                }
              }).commitNowAllowingStateLoss()
          }
        }
      }
    }
  }

  private fun handleUri(uri: Uri) {
    handler.post(object : Runnable {
      var count = 0
      override fun run() {
        context.contentResolver.query(uri, projection, null, null, null)?.takeIf { it.moveToFirst() }?.use {
          val index = it.getColumnIndex(projection[0])
          if (index > -1) {
            if (File(it.getString(index)).name.startsWith(".pending") && count++ < 5) {
              handler.postDelayed(this, 200)
            } else {
              ignoreUris.add(uri.toString())
              saveImg(context, uri)
            }
          }
        }
      }
    })
  }

  companion object {
    private val ignoreUris: MutableList<String> = mutableListOf()
    private const val permission = Manifest.permission.READ_EXTERNAL_STORAGE

    fun saveImg(context: Context, uri: Uri): Uri? {
      val time = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
      val values = contentValuesOf(
        MediaStore.MediaColumns.DISPLAY_NAME to "app_$time",
        MediaStore.Images.Media.MIME_TYPE to "image/jpeg"
      )
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.putAll(
          contentValuesOf(
            MediaStore.MediaColumns.RELATIVE_PATH to Environment.DIRECTORY_PICTURES,
            MediaStore.MediaColumns.IS_PENDING to true
          )
        )
      }
      return with(context.contentResolver) {
        insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let {
          Pair(it, openOutputStream(it))
        }?.let {
          runCatching { BitmapFactory.decodeStream(openInputStream(uri)) }.getOrNull()
            ?.compress(Bitmap.CompressFormat.JPEG, 100, it.second)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            update(it.first, values.apply {
              clear()
              put(MediaStore.MediaColumns.IS_PENDING, false)
            }, null, null)
          }
          MediaScannerConnection.scanFile(context, arrayOf(it.first.toString()), arrayOf("image/jpeg"), null)
          context.showToast("图片已保存到本地")
          ignoreUris.add(it.first.toString())
          it.first
        }
      }
    }

    class PermissionFragment : Fragment() {
      private var callback: ((Boolean) -> Unit)? = null
      fun setCallback(callback: (Boolean) -> Unit) = apply {
        this.callback = callback
      }

      override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
          callback?.invoke(it)
        }.launch(permission)
      }
    }
  }
}
