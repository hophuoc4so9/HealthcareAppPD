package com.example.healthcareapppd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.healthcareapppd.databinding.ActivityMainBinding
import com.example.healthcareapppd.presentation.ui.HomeFragment
import com.example.healthcareapppd.presentation.ui.NotificationFragment
import com.example.healthcareapppd.presentation.ui.ProfileFragment
import com.example.healthcareapppd.presentation.ui.ReportFragment
import com.example.healthcareapppd.presentation.ui.AuthFragment // thêm

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.navigation_home -> HomeFragment()
                R.id.navigation_report -> ReportFragment()
                R.id.navigation_notifications -> NotificationFragment()
                R.id.navigation_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            replaceFragment(selectedFragment)
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }
}
