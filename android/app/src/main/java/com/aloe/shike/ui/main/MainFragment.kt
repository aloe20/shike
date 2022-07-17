package com.aloe.shike.ui.main

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.aloe.shike.R
import com.aloe.shike.base.BasePageFragment
import com.aloe.shike.ui.navi.NaviFragment
import com.aloe.shike.ui.recommend.RecommendFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : BasePageFragment() {
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return FrameLayout(requireContext()).apply {
      val naviHeight = resources.getDimensionPixelSize(R.dimen.navigation_height)
      val pagerAdapter = getPagerAdapter(
        listOf(
          Triple(RecommendFragment(), R.string.recommend, R.drawable.ic_recommend),
          Triple(NaviFragment(), R.string.navigation, R.drawable.ic_navigation)
        )
      )
      val viewPager = ViewPager2(context).apply { adapter = pagerAdapter }
      val layerDrawable = LayerDrawable(arrayOf(ShapeDrawable(RectShape()).apply { paint.color = Color.GRAY })).apply {
        setLayerInsetBottom(0, naviHeight - 1)
      }
      val tabLayout = TabLayout(context).apply {
        setSelectedTabIndicator(ColorDrawable())
        background = layerDrawable
      }
      addView(
        viewPager,
        FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT).apply {
          bottomMargin = naviHeight
        })
      addView(tabLayout, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, naviHeight).apply {
        gravity = Gravity.BOTTOM
      })
      TabLayoutMediator(tabLayout, viewPager) { tab, position ->
        tab.setIcon(pagerAdapter.getDrawable(position))
        tab.setText(pagerAdapter.getTitle(position))
      }.attach()
    }
  }
}
