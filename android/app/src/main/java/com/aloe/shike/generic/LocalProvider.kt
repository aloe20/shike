package com.aloe.shike.generic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

fun lineModifier(start: Dp = 0.dp, top: Dp = 0.dp, end: Dp = 0.dp, bottom: Dp = 0.dp) = Modifier
  .fillMaxWidth()
  .padding(start, top, end, bottom)
  .height(0.3.dp)
  .background(Color(0x88888888))

val LocalNavController = compositionLocalOf<NavHostController> { error("LocalNavController not present") }
