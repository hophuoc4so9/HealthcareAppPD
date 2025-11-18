package com.example.healthcareapppd

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.healthcareapppd.presentation.ui.Auth.AuthFragment
import com.example.healthcareapppd.presentation.ui.LoginFragment

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dùng fragment container mặc định
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, AuthFragment()) // Mặc định mở màn hình Đăng ký
                .commit()
        }
    }

    // Điều hướng sang MainActivity sau khi đăng nhập thành công
    fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Chuyển từ Đăng ký sang Đăng nhập
    fun navigateToLogin() {
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, LoginFragment())
            .addToBackStack(null)
            .commit()
    }

}