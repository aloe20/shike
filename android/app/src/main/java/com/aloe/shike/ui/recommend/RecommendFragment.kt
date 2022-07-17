package com.aloe.shike.ui.recommend

import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aloe.shike.Purple40
import com.aloe.shike.R
import com.aloe.shike.base.BaseComposeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecommendFragment : BaseComposeFragment() {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Layout() {
    val state = rememberDrawerState(DrawerValue.Closed)
    var value by remember { mutableStateOf(0) }
    ModalNavigationDrawer(drawerContent = {
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
          ) {

          }
        }
        Spacer(modifier = Modifier
          .width(120.dp)
          .fillMaxHeight()
          .clickable {
            value++
          })
      }
    }, drawerState = state, gesturesEnabled = false, drawerContainerColor = Color.Transparent) {
      Scaffold(modifier = Modifier
        .systemBarsPadding(), topBar = {
        Box(modifier = Modifier.height(40.dp)){
          SmallTopAppBar(title = {
            Box(modifier = Modifier.fillMaxWidth()) {
              Text(text = stringResource(id = R.string.recommend),fontSize = 16.sp)
            }
          }, navigationIcon = {
            IconButton(onClick = {
              value++
            }) {
              Icon(imageVector = Icons.Rounded.Menu, contentDescription = "")
            }
          })
          Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(Color.Gray)
            .align(Alignment.BottomCenter))
        }
      }) {
        Column(modifier = Modifier.padding(it)) {
          Text(text = stringResource(R.string.recommend))
        }
      }
    }
    if (value > 0) {
      LaunchedEffect(key1 = value) {
        if (state.isOpen) {
          state.close()
        } else {
          state.open()
        }
      }
    }
  }
}
