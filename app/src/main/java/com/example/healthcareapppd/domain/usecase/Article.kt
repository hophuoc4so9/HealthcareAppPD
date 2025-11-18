package com.example.healthcareapppd.domain.usecase

data class Article(
    val title: String,
    val date: String,
    val imageResId: Int // lưu resource id ảnh (vd: R.drawable.ic_doctor)
)