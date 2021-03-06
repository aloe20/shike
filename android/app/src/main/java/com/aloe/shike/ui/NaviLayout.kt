package com.aloe.shike.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.aloe.shike.generic.DetailActivity
import com.aloe.shike.generic.Native

@Composable
fun NaviLayout() {
  val context = LocalContext.current
  Text(text = Native.hello(), modifier = Modifier.clickable {
    context.startActivity(Intent(context, DetailActivity::class.java))
  })
}
