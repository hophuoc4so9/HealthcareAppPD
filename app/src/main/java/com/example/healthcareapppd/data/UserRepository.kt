package com.example.healthcareapppd.data

import android.content.Context
import com.example.healthcareapppd.domain.usecase.User

class UserRepository(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    // Đăng ký
    fun registerUser(user: User): Boolean {
        return if (!dbHelper.isEmailExists(user.email)) {
            dbHelper.insertUser(user)
        } else {
            false // email đã tồn tại
        }
    }

    // Đăng nhập
    fun loginUser(email: String, password: String): Boolean {
        return dbHelper.checkUser(email, password)
    }
}

