package com.aloe.shike.ui.navi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aloe.shike.R
import com.aloe.shike.base.BaseComposeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NaviFragment:BaseComposeFragment() {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Layout() {
    Scaffold(modifier = Modifier.systemBarsPadding(), topBar = {
      SmallTopAppBar(title = {})
    }) {
      Column(modifier = Modifier.padding(it)) {
        Text(text = stringResource(R.string.navigation))
      }
    }
  }
}
