package com.example.healthcareapppd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.healthcareapppd.databinding.ActivityMainBinding
import com.example.healthcareapppd.presentation.ui.HomeFragment
import com.example.healthcareapppd.presentation.ui.NotificationFragment
import com.example.healthcareapppd.presentation.ui.ProfileFragment
import com.example.healthcareapppd.presentation.ui.ReportFragment
import com.example.healthcareapppd.ui.theme.HealthcareAppPDTheme

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
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

