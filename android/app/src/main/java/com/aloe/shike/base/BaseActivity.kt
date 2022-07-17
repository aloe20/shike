package com.aloe.shike.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewTreeLifecycleOwner
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class BaseActivity<T : Fragment> : AppCompatActivity() {
  private val types: Array<Type> = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments
  final override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    ViewTreeLifecycleOwner.set(window.decorView, this)
    @Suppress("UNCHECKED_CAST")
    supportFragmentManager.beginTransaction().setReorderingAllowed(true)
      .replace(android.R.id.content, types[0] as Class<Fragment>, null, null).commitNowAllowingStateLoss()
  }
}
