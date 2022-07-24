package com.aloe.shike.ui

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aloe.shike.generic.log
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

@OptIn(ExperimentalPagerApi::class)
@Composable
fun BannerLayout(height: Dp, list: List<String>) {
  val loopingCount = Int.MAX_VALUE
  val startIndex = loopingCount / 2
  val pagerState = rememberPagerState(initialPage = startIndex)
  fun pageMapper(index: Int) =
    if (list.isEmpty()) 0 else ((index - startIndex) - (index - startIndex).floorDiv(list.size) * list.size)
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight()
  ) {
    HorizontalPager(
      count = loopingCount,
      state = pagerState,
      modifier = Modifier
        .fillMaxWidth()
        .height(height)
    ) { index ->
      AsyncImage(
        model = list[pageMapper(index)],
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    }
    HorizontalPagerIndicator(
      pagerState = pagerState,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(8.dp),
      pageCount = list.size,
      pageIndexMapping = ::pageMapper,
      activeColor = Color.White
    )
    val loopState = remember { mutableStateOf(true) }
    var underDragging by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
      pagerState.interactionSource.interactions.collect { interaction ->
        when (interaction) {
          is PressInteraction.Press -> underDragging = true
          is PressInteraction.Release -> underDragging = false
          is PressInteraction.Cancel -> underDragging = false
          is DragInteraction.Start -> underDragging = true
          is DragInteraction.Stop -> underDragging = false
          is DragInteraction.Cancel -> underDragging = false
        }
      }
    }
    val looping = loopState.value
    if (underDragging.not() && looping) {
      LaunchedEffect(key1 = underDragging) {
        try {
          while (true) {
            delay(3000L)
            val current = pagerState.currentPage
            val currentPos = pageMapper(current)
            val nextPage = current + 1
            if (underDragging.not()) {
              val toPage = nextPage.takeIf { nextPage < pagerState.pageCount } ?: (currentPos + startIndex + 1)
              if (toPage > current) {
                pagerState.animateScrollToPage(toPage)
              } else {
                pagerState.scrollToPage(toPage)
              }
            }
          }
        } catch (e: CancellationException) {
          "Launched paging cancelled".log(tr = e)
        }
      }
    }
  }
}
