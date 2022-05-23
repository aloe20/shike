package com.aloe.shike.ui.main

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.metrics.performance.PerformanceMetricsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aloe.flu.FlutterLayout
import com.aloe.flu.paddingStatus
import com.aloe.rn.ReactLayout
import com.aloe.shike.app.AppTheme
import com.aloe.shike.app.Purple500
import com.aloe.shike.ui.excel.ExcelLayout
import com.aloe.shike.ui.rich.PdfContentView
import com.aloe.web.WebLayout
import com.aloe.web.routerWebPrefix
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

val LocalNavController = compositionLocalOf<NavHostController> { error("") }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paddingStatus.value = false
        setContent {
            val systemUiController = rememberSystemUiController()
            val useLightIcons = MaterialTheme.colors.isLight.not()
            val navController = rememberNavController()
            AppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    SideEffect { systemUiController.setStatusBarColor(Purple500, useLightIcons) }
                    val viewModel = hiltViewModel<MainVm>()
                    val state by viewModel.getUiState().collectAsState()
                    viewModel.loadUiData()
                    CompositionLocalProvider(LocalNavController provides navController) {
                        NavHost(navController = navController, startDestination = "main") {
                            composable("main") {
                                Column {
                                    if (paddingStatus.value) {
                                        Box(modifier = Modifier.statusBarsPadding())
                                    }
                                    MainLayout(bean = state.banners)
                                }
                            }
                            composable(
                                "$routerWebPrefix{url}",
                                arguments = listOf(navArgument("url") { nullable = true })
                            ) {
                                it.arguments?.getString("url")?.also { url ->
                                    if (Uri.parse(url).host == "flutter.cn") {
                                        FlutterLayout()
                                    } else {
                                        WebLayout(url = url) { navController.navigateUp() }
                                    }
                                }
                            }
                            composable("pdf") {
                                PdfContentView()
                            }
                            composable("web") {
                                WebLayout("http://192.168.1.4:3000/vue") { navController.navigateUp() }
                            }
                            composable("rn") {
                                ReactLayout {
                                    navController.navigateUp()
                                }
                            }
                            composable("list") {
                                ExcelLayout { navController.navigateUp() }
                            }
                        }
                    }
                    PrivacyDialog(visible = state.privacyVisible) {
                        viewModel.hidePrivacy()
                    }
                }
            }
        }
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        val stateHolder = PerformanceMetricsState.getForHierarchy(view)
        stateHolder.state?.addState("Activity", javaClass.simpleName)
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun PrivacyDialog(visible: Boolean, confirm: () -> Unit) {
    var showState by mutableStateOf(visible)
    if (showState) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    showState = false
                    confirm.invoke()
                }) { Text(text = "同意") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showState = false
                    Process.killProcess(Process.myPid())
                }) { Text(text = "不同意") }
            },
            title = { Text(text = "隐私协议") },
            text = { Text(text = "是否同意隐私协议") }
        )
    }
}
