package com.aloe.shike.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aloe.rn.ReactView
import com.aloe.shike.generic.LocalNavController
import com.aloe.shike.generic.lineModifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactLayout(url: String) {
  val navController = LocalNavController.current
  Scaffold(modifier = Modifier.statusBarsPadding()) {
    Column(modifier = Modifier.fillMaxSize()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
      ) {
        IconButton(
          onClick = { navController.navigateUp() },
          modifier = Modifier.size(44.dp)
        ) {
          Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "")
        }
        Text(text = "React Native", modifier = Modifier.align(Alignment.Center))
        Spacer(modifier = lineModifier().align(Alignment.BottomCenter))
      }
      AndroidView(
        factory = { ReactView(it).apply { setBackBtnHandler { navController.navigateUp() } } },
        modifier = Modifier.fillMaxSize()
      ) {
        it.loadPage(Uri.parse(url))
      }
    }
  }
}
