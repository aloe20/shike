package com.aloe.shike.simple

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aloe.shike.ui.MainLayout
import com.aloe.shike.ui.ReactLayout
import com.aloe.shike.ui.ZxingLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    setContent {
      val navController = rememberNavController()
      AppTheme {
        Surface(modifier = Modifier
          .fillMaxSize()
          .systemBarsPadding(), color = MaterialTheme.colorScheme.background) {
          CompositionLocalProvider(LocalNavController provides navController) {
            NavHost(navController = navController, startDestination = "main") {
              composable("main") { MainLayout() }
              composable("react"){ ReactLayout{navController.navigateUp()} }
              composable("scan"){ ZxingLayout(result = {
                Log.e("aloe", "--> $it")
                navController.navigateUp()
              })
              }
            }
          }
        }
      }
    }
  }
}
