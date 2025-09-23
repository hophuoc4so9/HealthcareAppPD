package com.example.healthcareapppd

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.healthcareapppd.presentation.ui.IntroduceFragment1
import com.example.healthcareapppd.presentation.ui.IntroduceFragment2
import com.example.healthcareapppd.presentation.ui.IntroduceFragment3
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var skipText: TextView
    private lateinit var nextButton: FloatingActionButton

    private val fragmentList = listOf(
        IntroduceFragment1(),
        IntroduceFragment2(),
        IntroduceFragment3()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_onboarding) // activity layout

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabIndicator)
        skipText = findViewById(R.id.tvSkip)
        nextButton = findViewById(R.id.btnNext)

        // Adapter gộp trong Activity
        viewPager.adapter = OnboardingAdapter(fragmentList)

        // Liên kết TabLayout với ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        // Skip → nhảy thẳng trang Welcome
        skipText.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Next → sang trang tiếp, nếu cuối → sang WelcomeActivity
        nextButton.setOnClickListener {
            if (viewPager.currentItem < fragmentList.size - 1) {
                viewPager.currentItem++
            } else {
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    // Adapter gộp trong Activity
    inner class OnboardingAdapter(
        private val fragments: List<Fragment>
    ) : FragmentStateAdapter(this@OnboardingActivity) {
        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }
}
