package com.aloe.shike.simple

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
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
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    setContent {
      AppTheme {
        Surface(
          modifier = Modifier
            .fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
          val navController = rememberNavController()
          CompositionLocalProvider(LocalNavController provides navController) {
            NavHost(navController = navController, startDestination = "main") {
              composable("main") { MainLayout() }
              composable("react") { ReactLayout { navController.navigateUp() } }
              composable("scan") {
                ZxingLayout(result = {
                  Log.e("aloe", "--> $it")
                  navController.navigateUp()
                })
              }
            }
          }
        }
      }
    }
    if(!Settings.canDrawOverlays(this)) {
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

      }.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package: $packageName")))
    }
  }
}
