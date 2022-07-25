package com.aloe.shike.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import com.aloe.shike.generic.LocalNavController
import com.aloe.shike.generic.lineModifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcelLayout() {
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
        factory = { RecyclerView(it) },
        modifier = Modifier.fillMaxSize()
      ) {

      }
    }
  }
}
