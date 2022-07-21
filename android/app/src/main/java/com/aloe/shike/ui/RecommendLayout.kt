package com.aloe.shike.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.aloe.shike.simple.Purple40
import com.aloe.shike.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun RecommendLayout() {
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
    Scaffold(modifier = Modifier
      .systemBarsPadding(), topBar = {
      Box(modifier = Modifier.height(40.dp)) {
        SmallTopAppBar(title = {
          Box(modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(id = R.string.recommend), fontSize = 16.sp)
          }
        }, navigationIcon = {
          IconButton(onClick = {
            value++
          }) {
            Icon(imageVector = Icons.Rounded.Menu, contentDescription = "")
          }
        })
        Spacer(
          modifier = Modifier
            .fillMaxWidth()
            .height(0.3.dp)
            .background(Color.Gray)
            .align(Alignment.BottomCenter)
        )
      }
    }) {
      Column(modifier = Modifier.padding(it)) {
        val pageCount = 10
        val loopingCount = Int.MAX_VALUE
        val startIndex = loopingCount / 2
        val pagerState = rememberPagerState(initialPage = startIndex)

        fun pageMapper(index: Int): Int {
          return (index - startIndex).floorMod(pageCount)
        }
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
        ) {
          val height = 160.dp
          HorizontalPager(
            count = loopingCount,
            state = pagerState,
            modifier = Modifier
              .fillMaxWidth()
              .height(height)
          ) { index ->
            AsyncImage(
              model = randomImageUrl(
                width = LocalContext.current.resources.displayMetrics.widthPixels,
                height = height.value.toInt(),
                seed = index
              ),
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
            pageCount = pageCount,
            pageIndexMapping = ::pageMapper
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
                Log.i("page", "Launched paging cancelled")
              }
            }
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

private fun Int.floorMod(other: Int): Int = when (other) {
  0 -> this
  else -> this - floorDiv(other) * other
}

private val rangeForRandom = (0..100000)

private fun randomImageUrl(
  width: Int,
  height: Int,
  seed: Int = rangeForRandom.random()
): String {
  return "https://picsum.photos/seed/$seed/$width/$height"
}
