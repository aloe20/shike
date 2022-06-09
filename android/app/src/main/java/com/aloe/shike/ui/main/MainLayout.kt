package com.aloe.shike.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.aloe.shike.ui.me.MeLayout
import com.aloe.shike.ui.recommend.RecommendLayout
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainLayout(bean: List<BannerBean>) {
    var title by remember { mutableStateOf("首页") }
    val pagerState = rememberPagerState(0)
    val scope = rememberCoroutineScope()
    val items = listOf("推荐", "导航", "项目", "文章")
    val nav = LocalNavController.current
    var showMenu by remember { mutableStateOf(false) }
    var isValid by remember { mutableStateOf(false) }
    var showDialog by mutableStateOf(false)
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
                3 -> MeLayout()
                else -> Text(text = "page $index", modifier = Modifier.fillMaxSize())
            }
        }
        val launcher: ActivityResultLauncher<String>? = LocalActivityResultRegistryOwner.current?.activityResultRegistry?.register(
            "camera",
            ActivityResultContracts.RequestPermission()
        ) { hasPermission ->
            if (isValid) {
                if (hasPermission) {
                    nav.navigate("scan")
                } else {
                    showDialog = true
                }
                isValid = false
            }
        }
        if (showDialog) {
            ShowDialog {
                showDialog = false
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
                    DropdownMenuItem(onClick = {
                        isValid = true
                        launcher?.launch(Manifest.permission.CAMERA)
                        showMenu = false
                    }) {
                        Text(text = "扫码")
                    }
                }
            }
        }
    }
}


@Composable
fun ShowDialog(dismissCallback: () -> Unit) {
    val launcher = LocalActivityResultRegistryOwner.current?.activityResultRegistry?.register(
        "camera",
        ActivityResultContracts.StartActivityForResult()
    ) {}
    AlertDialog(
        onDismissRequest = dismissCallback,
        confirmButton = {
            TextButton(onClick = {
                launcher?.launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.aloe.shike")))
                dismissCallback.invoke()
            }) { Text(text = "是") }
        },
        dismissButton = {
            TextButton(onClick = dismissCallback) { Text(text = "否") }
        },
        title = { Text(text = "打开权限") },
        text = { Text(text = "是否到设置页面打开相机权限？") }
    )
}
