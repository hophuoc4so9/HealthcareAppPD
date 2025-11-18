package com.example.healthcareapppd.data

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREF_NAME = "healthcare_session"
    private const val KEY_EMAIL = "user_email"

    fun saveUserEmail(context: Context, email: String) {
        val sharedPref: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().putString(KEY_EMAIL, email).apply()
    }

    fun getUserEmail(context: Context): String? {
        val sharedPref: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_EMAIL, null)
    }

    fun clearSession(context: Context) {
        val sharedPref: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
    }
}


