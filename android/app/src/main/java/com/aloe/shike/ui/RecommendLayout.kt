package com.aloe.shike.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.aloe.shike.R
import com.aloe.shike.generic.LocalNavController
import com.aloe.shike.generic.Purple40
import com.aloe.shike.generic.lineModifier
import com.aloe.shike.generic.log
import com.aloe.shike.vm.RecommendVm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendLayout() {
  val context = LocalContext.current
  val navController = LocalNavController.current
  val state = rememberDrawerState(DrawerValue.Closed)
  var value by remember { mutableStateOf(0) }
  var showDialog by remember { mutableStateOf(false) }
  val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
    if (it) navController.navigate("scan") else showDialog = true
  }
  val resultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
      navController.navigate("scan")
    }
  }
  if (showDialog) {
    ShowDialog {
      showDialog = false
      if (it) {
        resultLauncher.launch(
          Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        )
      }
    }
  }
  ModalNavigationDrawer(drawerContent = {
    DrawerContentLayout(navController) { value++ }
  }, drawerState = state, gesturesEnabled = false, drawerContainerColor = Color.Transparent) {
    DrawerContainerLayout(menuClick = { value++ }) {
      launcher.launch(Manifest.permission.CAMERA)
    }
  }
  if (value > 0) {
    LaunchedEffect(value) {
      if (state.isOpen) state.close() else state.open()
    }
  }
}

@Composable
fun DrawerContentLayout(navController: NavHostController, onClick: () -> Unit) {
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
      Text(text = "React Native", modifier = Modifier
        .fillMaxWidth()
        .clickable {
          appNavigate(navController, Page.React.setUrl("assets://index.bundle"))
          onClick.invoke()
        }
        .padding(16.dp, 8.dp)
      )
      Spacer(modifier = lineModifier())
      Text(text = "Js Bridge", modifier = Modifier
        .fillMaxWidth()
        .clickable {
          appNavigate(navController, Page.Web.setUrl("file:///android_asset/bridge.html"))
          onClick.invoke()
        }
        .padding(16.dp, 8.dp)
      )
      Spacer(modifier = lineModifier())
      Text(text = "Flutter", modifier = Modifier
        .fillMaxWidth()
        .clickable {
          appNavigate(navController, Page.Flutter)
          onClick.invoke()
        }
        .padding(16.dp, 8.dp)
      )
      Spacer(modifier = lineModifier())
    }
    Spacer(
      modifier = Modifier
        .width(120.dp)
        .fillMaxHeight()
        .clickable(onClick = onClick)
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContainerLayout(menuClick: () -> Unit, scanClick: () -> Unit) {
  val vm = viewModel<RecommendVm>()
  val uiState = vm.getUiState().observeAsState()
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
        IconButton(
          modifier = Modifier
            .systemBarsPadding()
            .size(44.dp), onClick = menuClick
        ) {
          Icon(imageVector = Icons.Rounded.Menu, contentDescription = "", tint = Color.White)
        }
        IconButton(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .systemBarsPadding()
            .size(44.dp), onClick = scanClick
        ) {
          Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_scan),
            contentDescription = "",
            tint = Color.White
          )
        }
      }
      uiState.value?.banner?.also { list ->
        Text(text = "$list")
      }
    }
  }
  if (!vm.isBannerLoaded) {
    vm.loadData()
  }
}

@Composable
fun ShowDialog(click: (Boolean) -> Unit) {
  AlertDialog(
    onDismissRequest = { },
    confirmButton = {
      TextButton(onClick = {
        click.invoke(true)
      }) {
        Text(text = "去设置")
      }
    },
    dismissButton = {
      TextButton(onClick = { click.invoke(false) }) { Text(text = "取消") }
    },
    title = { Text(text = "提示") },
    text = { Text(text = "扫二维码需要相机权限，去设置页面打开权限") })
}

private val rangeForRandom = (0..100000)

private fun randomImageUrl(width: Int, height: Int, seed: Int = rangeForRandom.random()) =
  "https://picsum.photos/seed/$seed/$width/$height"
