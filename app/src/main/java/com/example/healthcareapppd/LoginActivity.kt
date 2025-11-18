package com.example.healthcareapppd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.healthcareapppd.presentation.ui.Auth.AuthFragment
import com.example.healthcareapppd.presentation.ui.LoginFragment

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dùng fragment_login.xml
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, LoginFragment())
            .commit()
    }

    fun navigateToAuth() {
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, AuthFragment())
            .addToBackStack(null)
            .commit()
    }
}
