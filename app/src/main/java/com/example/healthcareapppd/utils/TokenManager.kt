package com.example.healthcareapppd.utils

import android.content.Context

class TokenManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("HealthcareApp", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        
        private var instance: TokenManager? = null
        
        fun init(context: Context): TokenManager {
            if (instance == null) {
                instance = TokenManager(context)
            }
            return instance!!
        }
        
        fun getToken(context: Context): String? {
            if (instance == null) {
                instance = TokenManager(context)
            }
            return instance?.getToken()
        }
        
        fun getInstance(): TokenManager? = instance
    }

    fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun saveUserInfo(userId: String, email: String, role: String) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ROLE, role)
        }.apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    fun getUserRole(): String? {
        return sharedPreferences.getString(KEY_USER_ROLE, null)
    }

    fun isTokenValid(): Boolean {
        return !getToken().isNullOrEmpty()
    }

    fun clearToken() {
        sharedPreferences.edit().apply {
            remove(KEY_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_ROLE)
        }.apply()
    }
}
