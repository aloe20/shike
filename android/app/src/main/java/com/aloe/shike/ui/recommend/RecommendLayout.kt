package com.aloe.shike.ui.recommend

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.aloe.bean.BannerBean
import com.aloe.shike.ui.main.LocalNavController
import com.aloe.web.routerWebPrefix
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.*

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalPagerApi::class)
@Composable
fun RecommendLayout(bean: List<BannerBean>) {
  val index = remember { mutableStateOf(Int.MAX_VALUE.shr(1)) }
  val pagerState = rememberPagerState(index.value)
  val nav = LocalNavController.current
  val scope = rememberCoroutineScope()
  Column(modifier = Modifier.fillMaxHeight()) {
    if (bean.isNotEmpty()) {
      val indicatorState = rememberPagerState(0)
      Box {
        HorizontalPager(
          count = Int.MAX_VALUE,
          modifier = Modifier.height(200.dp),
          state = pagerState,
          userScrollEnabled = false
        ) { index ->
          Box {
            Image(
              painter = rememberAsyncImagePainter(model = bean[index % bean.size].imagePath),
              contentDescription = null,
              modifier = Modifier
                .fillMaxSize()
                .clickable {
                  nav.navigate(routerWebPrefix + bean[index % bean.size].url) {}
                },
              contentScale = ContentScale.Crop
            )
            Text(
              text = bean[index % bean.size].title ?: "",
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x33FFFFFF))
                .align(Alignment.BottomStart)
                .padding(start = 8.dp, bottom = 8.dp)
            )
          }
        }
        HorizontalPager(
          count = bean.size, modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 8.dp, bottom = 8.dp), state = indicatorState,
          userScrollEnabled = false
        ) {}
        HorizontalPagerIndicator(
          pagerState = indicatorState,
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 8.dp, bottom = 8.dp)
        )
      }
      scope.launch {
        indicatorState.scrollToPage(if (indicatorState.pageCount == 0) 0 else pagerState.currentPage % indicatorState.pageCount)
        delay(3000)
        index.value = pagerState.currentPage + 1
        pagerState.animateScrollToPage(index.value)
        indicatorState.scrollToPage(if (indicatorState.pageCount == 0) 0 else pagerState.currentPage % indicatorState.pageCount)
      }
    }
  }
}
