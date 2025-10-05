package com.example.healthcareapppd

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.healthcareapppd.presentation.ui.auth.AuthFragment

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dùng fragment_auth.xml
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, AuthFragment())
            .commit()
    }

    // Hàm này sẽ được gọi từ AuthFragment khi login thành công
    fun navigateToMain() {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
