package com.aloe.shike.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aloe.shike.R
import com.aloe.shike.generic.lineModifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainLayout() {
  val list = listOf(
    Pair(R.string.recommend, R.drawable.ic_recommend), Pair(R.string.navigation, R.drawable.ic_navigation)
  )
  val scope = rememberCoroutineScope()
  Box(modifier = Modifier.fillMaxSize()) {
    val state = rememberPagerState()
    var statusBarDarkIcons by mutableStateOf(state.currentPage == 1)
    HorizontalPager(
      count = list.size, modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 56.dp), state = state
    ) {
      when (it) {
        0 -> RecommendLayout()
        1 -> NaviLayout()
      }
    }
    BottomNavigation(
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .align(Alignment.BottomCenter),
      backgroundColor = MaterialTheme.colorScheme.background,
      elevation = 0.dp
    ) {
      list.forEachIndexed { index, pair ->
        BottomNavigationItem(
          selected = state.currentPage == index,
          onClick = {
            statusBarDarkIcons = index == 1
            scope.launch {
              state.animateScrollToPage(index)
            }
          },
          icon = {
            Icon(
              imageVector = ImageVector.vectorResource(id = pair.second),
              contentDescription = null,
              modifier = Modifier.size(24.dp),
              tint = if (state.currentPage == index) MaterialTheme.colorScheme.primary else Color.Gray
            )
          },
          label = {
            Text(
              stringResource(pair.first),
              modifier = Modifier.padding(top = 8.dp),
              color = if (state.currentPage == index) MaterialTheme.colorScheme.primary else Color.Gray,
              fontSize = 12.sp
            )
          },
        )
      }
    }
    Spacer(modifier = lineModifier(bottom = 55.7.dp).align(Alignment.BottomCenter))
    val systemUiController = rememberSystemUiController()
    LaunchedEffect(systemUiController, statusBarDarkIcons) {
      systemUiController.statusBarDarkContentEnabled = statusBarDarkIcons
    }
  }
}
