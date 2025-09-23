package com.example.healthcareapppd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_welcome) // file XML bạn gửi

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSignup = findViewById<MaterialButton>(R.id.btnSignup)

        // Nút Đăng nhập
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Nút Đăng ký
        btnSignup.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
    }
}
