package com.aloe.shike.base

import androidx.viewpager2.adapter.FragmentStateAdapter

abstract class BasePageFragment : BaseFragment() {
  fun getPagerAdapter(list: List<Triple<BaseFragment, Int, Int>>): SimpleAdapter = SimpleAdapter(this, list)
  inner class SimpleAdapter(fragment: BaseFragment, private val list: List<Triple<BaseFragment, Int, Int>>) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount() = list.size

    override fun createFragment(position: Int) = list[position].first

    fun getTitle(position: Int) = list[position].second

    fun getDrawable(position: Int) = list[position].third
  }
}
