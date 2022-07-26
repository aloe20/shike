package com.aloe.shike.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aloe.shike.generic.LocalNavController
import com.aloe.shike.generic.log

@Composable
fun HostLayout() {
  val navController = LocalNavController.current
  NavHost(navController = navController, startDestination = Page.Main.name) {
    composable(Page.Main.name) { MainLayout() }
    composable(Page.Flutter.name) { FlutterLayout() }
    composable(Page.React.name, arguments = listOf(navArgument("url") { type = NavType.StringType })) {
      it.arguments?.getString("url")?.also { url ->
        ReactLayout(url)
      }
    }
    composable(Page.Web.name, arguments = listOf(navArgument("url") { type = NavType.StringType })) {
      it.arguments?.getString("url")?.also { url ->
        WebLayout(url)
      }
    }
    composable(Page.Scan.name) {
      ZxingLayout(result = {
        navController.apply { it.log() }.navigateUp()
      })
    }
  }
}

fun appNavigate(controller: NavController, page: Page) {
  var route = page.name
  if (page is Page.React) {
    if (page.url.isNullOrBlank()) {
      "please set url to react page".log(tr = NullPointerException("url is null"))
      return
    }
    route = "react?url=${page.url}"
  } else if (page is Page.Web) {
    if (page.url.isNullOrBlank()) {
      "please set url to web page".log(tr = NullPointerException("url is null"))
      return
    }
    route = "web?url=${page.url}"
  }
  controller.navigate(route)
}

sealed class Page(var name: String) {
  object Main : Page("main")
  object Flutter : Page("flutter")
  object React : Page("react?url={url}") {
    var url: String? = null
      private set

    fun setUrl(url: String) = apply {
      this.url = url
    }
  }

  object Web : Page("web?url={url}") {
    var url: String? = null
      private set

    fun setUrl(url: String) = apply {
      this.url = url
    }
  }

  object Scan : Page("scan")
}
