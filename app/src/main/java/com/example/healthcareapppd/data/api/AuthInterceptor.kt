package com.example.healthcareapppd.data.api

import android.content.Context
import android.content.Intent
import com.example.healthcareapppd.WelcomeActivity
import com.example.healthcareapppd.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Nếu nhận 401 hoặc 403, token hết hạn hoặc không hợp lệ
        if (response.code == 401 || response.code == 403) {
            // Xóa token và chuyển về màn hình đăng nhập
            val tokenManager = TokenManager(context)
            tokenManager.clearToken()
            
            // Chuyển về WelcomeActivity
            val intent = Intent(context, WelcomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }

        return response
    }
}
