package com.example.healthcareapppd

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.databinding.ActivityMainBinding
import com.example.healthcareapppd.utils.TokenManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("MainActivity", "onCreate called")

        // 1. Xin quyền thông báo (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        // 2. Khởi tạo Dependencies
        tokenManager = TokenManager(this)
        RetrofitClient.init(this)

        // 3. Setup Navigation Controller
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 4. Kiểm tra đăng nhập
        if (!tokenManager.isTokenValid()) {
            navigateToWelcome()
            return
        } else {
            verifyTokenWithServer()
        }

        // 5. Setup Bottom Navigation
        setupBottomNavigation()

        // 6. LOGIC CHUYỂN HƯỚNG MÀN HÌNH CHÍNH (REDIRECT)
        if (savedInstanceState == null) {
            val role = tokenManager.getUserRole()

            if (role == "doctor") {
                // Logic cho Bác sĩ
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.navigation_home, true)
                    .build()
                try {
                    navController.navigate(R.id.doctorHomeFragment, null, navOptions)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Lỗi chuyển hướng bác sĩ: ${e.message}")
                }
            }
            // --- THÊM LOGIC CHO ADMIN TẠI ĐÂY ---
            else if (role == "admin") {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.navigation_home, true)
                    .build()
                try {
                    // Chuyển hướng sang Admin Home
                    navController.navigate(R.id.navigation_home_admin, null, navOptions)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Lỗi chuyển hướng admin: ${e.message}")
                }
            }
            // Nếu là User thì mặc định vào navigation_home (không cần else)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = binding.bottomNavigationView
        val role = tokenManager.getUserRole()

        // 1. Inflate menu đúng theo role
        bottomNav.menu.clear()
        when (role) {
            "doctor" -> bottomNav.inflateMenu(R.menu.bottom_nav_menu_doctor)
            "admin" -> bottomNav.inflateMenu(R.menu.bottom_nav_menu_admin) // Menu Admin
            else -> bottomNav.inflateMenu(R.menu.bottom_nav_menu) // Menu User
        }

        bottomNav.setupWithNavController(navController)

        // 2. Xử lý Ẩn/Hiện BottomBar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // --- NHÓM USER ---
                R.id.navigation_home,
                R.id.navigation_reports,
                R.id.navigation_profile,
                R.id.navigation_notification,
                R.id.navigation_messageListFragment,

                    // --- NHÓM DOCTOR ---
                R.id.doctorHomeFragment,
                R.id.navigation_reports_doctor,
                R.id.navigation_profile_doctor,

                    // --- NHÓM ADMIN (THÊM VÀO ĐÂY) ---
                R.id.navigation_home_admin,
                R.id.navigation_reports_admin -> {
                    bottomNav.visibility = View.VISIBLE
                }

                // Các màn hình khác -> Ẩn Menu
                else -> {
                    bottomNav.visibility = View.GONE
                }
            }
        }
    }

    private fun verifyTokenWithServer() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.authApi.getProfile("Bearer $token")

                if (!response.success || response.data == null) {
                    Log.e("MainActivity", "Token expired or invalid")
                    tokenManager.clearToken()
                    navigateToWelcome()
                } else {
                    Log.d("MainActivity", "Token verified")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error verifying token", e)
            }
        }
    }

    private fun navigateToWelcome() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}