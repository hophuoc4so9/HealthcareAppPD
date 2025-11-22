package com.example.healthcareapppd

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.healthcareapppd.utils.PreferencesManager
import com.example.healthcareapppd.utils.TokenManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_splash)

        val preferencesManager = PreferencesManager(this)
        val tokenManager = TokenManager(this)

        // Delay 3 giây trước khi kiểm tra và chuyển màn hình
        Handler(Looper.getMainLooper()).postDelayed({
            val nextActivityClass = when {
                // Lần đầu tiên: vào Onboarding (Introduce)
                preferencesManager.isFirstLaunch() -> OnboardingActivity::class.java
                // Đã xem introduce, kiểm tra token
                tokenManager.isTokenValid() -> MainActivity::class.java
                // Token hết hạn hoặc chưa đăng nhập: vào WelcomeActivity
                else -> WelcomeActivity::class.java
            }

            val intent = Intent(this, nextActivityClass)
            startActivity(intent)
            finish() // đóng splash để không quay lại bằng nút back
        }, 3000) // 3000ms = 3s
    }
}
