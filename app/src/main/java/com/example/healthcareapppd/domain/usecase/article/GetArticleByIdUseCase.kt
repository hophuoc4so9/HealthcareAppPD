package com.example.healthcareapppd.domain.usecase.article

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.Article
import com.example.healthcareapppd.data.api.model.ApiResponse

class GetArticleByIdUseCase {
    private val articleApi = RetrofitClient.articleApi
    
    suspend operator fun invoke(articleId: String): Result<Article> {
        return try {
            val response = articleApi.getArticleById(articleId)
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
