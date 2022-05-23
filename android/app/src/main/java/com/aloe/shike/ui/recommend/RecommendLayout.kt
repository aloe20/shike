package com.aloe.shike.ui.recommend

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.aloe.bean.BannerBean
import com.aloe.shike.ui.main.LocalNavController
import com.aloe.web.routerWebPrefix
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun RecommendLayout(bean: List<BannerBean>) {
  val pagerState = rememberPagerState()
  val nav = LocalNavController.current
  Column(modifier = Modifier.fillMaxHeight()) {
    Box {
      HorizontalPager(count = bean.size, modifier = Modifier.height(200.dp), state = pagerState) { index ->
        Box {
          Image(
            painter = rememberAsyncImagePainter(model = bean[index].imagePath),
            contentDescription = null,
            modifier = Modifier
              .fillMaxSize()
              .clickable {
                nav.navigate(routerWebPrefix + bean[index].url) {}
              }
          )
          Text(
            text = bean[index].title ?: "",
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0x33FFFFFF))
              .align(Alignment.BottomStart)
              .padding(start = 8.dp, bottom = 8.dp)
          )
        }
      }
      HorizontalPagerIndicator(
        pagerState = pagerState,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 8.dp, bottom = 8.dp)
      )
    }
  }
}
