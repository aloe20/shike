package com.aloe.shike.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aloe.shike.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainLayout() {
  val list =
    listOf(Pair(R.string.recommend, R.drawable.ic_recommend), Pair(R.string.navigation, R.drawable.ic_navigation))
  val scope = rememberCoroutineScope()
  Box(modifier = Modifier.fillMaxSize()) {
    val state = rememberPagerState()
    HorizontalPager(
      count = list.size, modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 56.dp), state = state
    ) {
      when(it){
        0->Text(text = "ITEM0")
        1->Text(text = "ITEM1")
      }
    }
    BottomNavigation(
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .align(Alignment.BottomCenter)
    ) {
      list.forEachIndexed { index, pair ->
        BottomNavigationItem(
          selected = state.currentPage == index,
          onClick = {
            scope.launch {
              state.animateScrollToPage(index)
            }
          },
          icon = {
            Icon(
              imageVector = ImageVector.vectorResource(id = pair.second),
              contentDescription = null,
              modifier = Modifier.size(24.dp),
              tint = if (state.currentPage == index) Color.White else Color.Gray
            )
          },
          label = { Text(stringResource(pair.first), modifier = Modifier.padding(top = 8.dp), color = if (state.currentPage == index) Color.White else Color.Gray, fontSize = 12.sp) },
        )
      }
    }
  }
}
