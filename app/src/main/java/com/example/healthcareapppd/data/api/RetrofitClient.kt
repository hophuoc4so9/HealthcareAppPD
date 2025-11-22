package com.example.healthcareapppd.data.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://be-healthcareapppd.onrender.com/"
    
    private var appContext: Context? = null
    
    fun init(context: Context) {
        appContext = context.applicationContext
    }
    
    // OkHttpClient with timeout configuration for slow server startup
    private val okHttpClient: OkHttpClient
        get() = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .apply {
                appContext?.let { context ->
                    addInterceptor(AuthInterceptor(context))
                }
            }
            .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
    
    val patientApi: PatientApiService by lazy {
        retrofit.create(PatientApiService::class.java)
    }
    
    val doctorApi: DoctorApiService by lazy {
        retrofit.create(DoctorApiService::class.java)
    }
    
    val appointmentApi: AppointmentApiService by lazy {
        retrofit.create(AppointmentApiService::class.java)
    }
    
    val facilityApi: FacilitiesApiService by lazy {
        retrofit.create(FacilitiesApiService::class.java)
    }
    
    val reminderApi: ReminderApiService by lazy {
        retrofit.create(ReminderApiService::class.java)
    }
    
    val chatApi: ChatApiService by lazy {
        retrofit.create(ChatApiService::class.java)
    }
    
    val articleApi: ArticleApiService by lazy {
        retrofit.create(ArticleApiService::class.java)
    }
    
    // Deprecated: use facilityApi instead
    @Deprecated("Use facilityApi instead", ReplaceWith("facilityApi"))
    val instance: FacilitiesApiService by lazy {
        facilityApi
    }
}