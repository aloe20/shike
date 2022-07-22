package com.aloe.shike.ui

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aloe.shike.simple.LocalNavController

@Composable
fun NaviLayout() {
  val navController = LocalNavController.current
  Text(text = "ITEM1", modifier = Modifier.clickable {
    navController.navigate("react")
  })
}
