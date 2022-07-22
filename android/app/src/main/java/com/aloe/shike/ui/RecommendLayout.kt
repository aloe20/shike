package com.aloe.shike.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aloe.shike.R
import com.aloe.shike.simple.Purple40
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendLayout() {
  val systemUiController = rememberSystemUiController()
  val statusBarDarkIcons by remember { mutableStateOf(false) }
  val navigationBarDarkIcons by remember { mutableStateOf(false) }

  LaunchedEffect(systemUiController, statusBarDarkIcons, navigationBarDarkIcons) {
    systemUiController.statusBarDarkContentEnabled = statusBarDarkIcons
    systemUiController.navigationBarDarkContentEnabled = navigationBarDarkIcons
  }
  val state = rememberDrawerState(DrawerValue.Closed)
  var value by remember { mutableStateOf(0) }
  ModalNavigationDrawer(drawerContent = {
    Row(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier
          .fillMaxHeight()
          .weight(1F)
          .clip(RoundedCornerShape(0.dp, 12.dp, 12.dp, 0.dp))
          .background(Color.White)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(Purple40)
        ) {}
      }
      Spacer(modifier = Modifier
        .width(120.dp)
        .fillMaxHeight()
        .clickable {
          value++
        })
    }
  }, drawerState = state, gesturesEnabled = false, drawerContainerColor = Color.Transparent) {
    Scaffold(modifier = Modifier.fillMaxSize()) {
      Column(modifier = Modifier.padding(it)) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
        ) {
          val height = 200.dp
          BannerLayout(height = height, list = List(4) { index ->
            randomImageUrl(
              width = LocalContext.current.resources.displayMetrics.widthPixels,
              height = height.value.toInt(),
              seed = index + 1
            )
          })
          IconButton(modifier = Modifier
            .systemBarsPadding()
            .size(44.dp), onClick = {
            value++
          }) {
            Icon(imageVector = Icons.Rounded.Menu, contentDescription = "", tint = Color.White)
          }
        }
      }
    }
  }
  if (value > 0) {
    LaunchedEffect(key1 = value) {
      if (state.isOpen) {
        state.close()
      } else {
        state.open()
      }
    }
  }
}

private val rangeForRandom = (0..100000)

private fun randomImageUrl(
  width: Int,
  height: Int,
  seed: Int = rangeForRandom.random()
): String {
  return "https://picsum.photos/seed/$seed/$width/$height"
}
