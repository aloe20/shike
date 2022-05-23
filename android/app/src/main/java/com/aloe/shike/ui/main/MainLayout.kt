package com.aloe.shike.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aloe.bean.BannerBean
import com.aloe.shike.R
import com.aloe.shike.app.Black3
import com.aloe.shike.app.Purple500
import com.aloe.shike.ui.recommend.RecommendLayout
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainLayout(bean: List<BannerBean>) {
    var title by remember { mutableStateOf("首页") }
    val pagerState = rememberPagerState(0)
    val scope = rememberCoroutineScope()
    val items = listOf("推荐", "导航", "项目", "文章")
    val nav = LocalNavController.current
    var showMenu by remember { mutableStateOf(false) }
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = title, fontSize = 16.sp) }, modifier = Modifier.height(40.dp), actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "")
            }
        })
    }, bottomBar = {
        BottomNavigation(backgroundColor = Color.White) {
            items.forEachIndexed { index, item ->
                BottomNavigationItem(
                    selected = index == pagerState.currentPage,
                    onClick = {
                        title = if (index == 0) "首页" else items[index]
                        scope.launch { pagerState.scrollToPage(index) }
                    },
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_android),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(if (pagerState.currentPage == index) Purple500 else Black3)
                        )
                    },
                    label = { Text(text = item) },
                    selectedContentColor = Purple500,
                    unselectedContentColor = Black3
                )
            }
        }
    }) {
        HorizontalPager(count = items.size, modifier = Modifier.padding(it), state = pagerState) { index ->
            when (index) {
                0 -> RecommendLayout(bean = bean)
                else -> Text(text = "page $index", modifier = Modifier.fillMaxSize())
            }
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    offset = DpOffset(4.dp, 0.dp)
                ) {
                    DropdownMenuItem(onClick = { nav.navigate("pdf") }) {
                        Text(text = "PDF")
                    }
                    DropdownMenuItem(onClick = { nav.navigate("web") }) {
                        Text(text = "网页")
                    }
                    DropdownMenuItem(onClick = { nav.navigate("rn") }) {
                        Text(text = "RN")
                    }
                    DropdownMenuItem(onClick = { nav.navigate("list") }) {
                        Text(text = "列表")
                    }
                }
            }
        }
        /*var sliderValue by remember { mutableStateOf(0F) }
        Slider(value = sliderValue, onValueChange = {
            sliderValue = it
            Log.e("aloe", "---> $sliderValue")
        }, valueRange = 0F..100F)*/
    }
}
