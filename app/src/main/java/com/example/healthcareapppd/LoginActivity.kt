package com.example.healthcareapppd

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.healthcareapppd.presentation.ui.LoginFragment

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dùng fragment_login.xml
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, LoginFragment())
            .commit()
    }

    // Hàm này sẽ được gọi từ AuthFragment khi login thành công
    fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Đóng AuthActivity, không cho quay lại
    }
}
