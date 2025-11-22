package com.example.healthcareapppd.domain.usecase.article

import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.Article
import com.example.healthcareapppd.data.api.model.ArticlesResponse

class GetAllArticlesUseCase {
    private val articleApi = RetrofitClient.articleApi
    
    suspend operator fun invoke(
        page: Int = 1,
        limit: Int = 10,
        status: String? = "published"
    ): Result<ArticlesResponse> {
        return try {
            val response = articleApi.getArticles(page, limit, status)
            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception("Get articles failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
