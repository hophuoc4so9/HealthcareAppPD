package com.example.healthcareapppd

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.healthcareapppd.databinding.ActivityMainBinding
import com.example.healthcareapppd.utils.TokenManager
import com.example.healthcareapppd.data.api.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
            // Xin quyền thông báo trên Android 13+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
                }
            }

        tokenManager = TokenManager(this)
        RetrofitClient.init(this)

        // Kiểm tra token
        if (!tokenManager.isTokenValid()) {
            // Chưa đăng nhập → quay về WelcomeActivity
            navigateToWelcome()
            return
        }
        
        // Verify token với server
        verifyTokenWithServer()

        // 1. Tìm NavController từ NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 2. Tự động liên kết BottomNavigationView với NavController
        binding.bottomNavigationView.setupWithNavController(navController)
        
        // 3. Hiển thị bottom navbar vì đã đăng nhập
        binding.bottomNavigationView.visibility = View.VISIBLE
    }
    
    private fun verifyTokenWithServer() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.authApi.getProfile("Bearer $token")
                
                if (!response.success || response.data == null) {
                    // Token không hợp lệ
                    tokenManager.clearToken()
                    navigateToWelcome()
                }
                // Token hợp lệ, tiếp tục sử dụng app
            } catch (e: Exception) {
                // Lỗi kết nối hoặc token hết hạn
                tokenManager.clearToken()
                navigateToWelcome()
            }
        }
    }
    
    private fun navigateToWelcome() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}