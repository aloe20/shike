package com.aloe.shike.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aloe.shike.AppTheme
import dagger.hilt.android.AndroidEntryPoint

lateinit var LocalNavController: ProvidableCompositionLocal<NavHostController>

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    LocalNavController = compositionLocalOf { NavHostController(this) }
    setContent {
      val controller = rememberNavController()
      AppTheme {
        Surface(modifier = Modifier
          .fillMaxSize()
          .systemBarsPadding(), color = MaterialTheme.colorScheme.background) {
          CompositionLocalProvider(LocalNavController provides controller) {
            NavHost(navController = controller, startDestination = "main1") {
              composable("main1") { MainLayout() }
              composable("main2") { MainLayout2() }
            }
          }
        }
      }
    }
  }
}



@Composable
fun MainLayout1() {
  val controller = LocalNavController.current
  Text(text = "第一页", modifier = Modifier.clickable {
    controller.navigate("main2")
  })
}

@Composable
fun MainLayout2() {
  val controller = LocalNavController.current
  Text(text = "第二页", modifier = Modifier.clickable {
    controller.navigateUp()
  })
}
