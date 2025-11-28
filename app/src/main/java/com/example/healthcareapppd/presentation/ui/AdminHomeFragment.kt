package com.example.healthcareapppd.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.healthcareapppd.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class AdminHomeFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)
        tabLayout = view.findViewById(R.id.tabLayoutAdmin)
        viewPager = view.findViewById(R.id.viewPagerAdmin)

        setupViewPager()
        return view
    }

    private fun setupViewPager() {
        val adapter = AdminPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Users"      // Tab 1: Người dùng
//                1 -> "Doctors"    // Tab 2: Bác sĩ
                1 -> "Articles"   // Tab 3: Bài viết (Full CRUD)
                else -> ""
            }
        }.attach()
    }
}

class AdminPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3 // Giảm xuống còn 3 tab

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AdminUsersFragment()
//            1 -> AdminDoctorsFragment()
            1 -> AdminArticlesFragment()
            else -> AdminUsersFragment()
        }
    }
}