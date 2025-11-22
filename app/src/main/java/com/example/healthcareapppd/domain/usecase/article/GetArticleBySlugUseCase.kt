package com.example.healthcareapppd.domain.usecase.article

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.Article

class GetArticleBySlugUseCase {
    private val articleApi = RetrofitClient.articleApi
    
    suspend operator fun invoke(slug: String): Result<Article> {
        return try {
            val response = articleApi.getArticleBySlug(slug)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Get article failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
