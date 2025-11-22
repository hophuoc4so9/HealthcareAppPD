package com.example.healthcareapppd.data.api

import com.example.healthcareapppd.data.api.model.*
import retrofit2.http.*

interface ArticleApiService {
    
    @GET("api/articles")
    suspend fun getArticles(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("status") status: String? = null
    ): ArticlesResponse
    
    @GET("api/articles/slug/{slug}")
    suspend fun getArticleBySlug(
        @Path("slug") slug: String
    ): ApiResponse<Article>
    
    @GET("api/articles/{id}")
    suspend fun getArticleById(
        @Path("id") articleId: String
    ): ApiResponse<Article>
    
    @POST("api/articles")
    suspend fun createArticle(
        @Header("Authorization") token: String,
        @Body request: CreateArticleRequest
    ): ApiResponse<Article>
    
    @PUT("api/articles/{id}")
    suspend fun updateArticle(
        @Header("Authorization") token: String,
        @Path("id") articleId: String,
        @Body request: CreateArticleRequest
    ): ApiResponse<Article>
    
    @PATCH("api/articles/{id}/publish")
    suspend fun publishArticle(
        @Header("Authorization") token: String,
        @Path("id") articleId: String
    ): ApiResponse<Article>
    
    @DELETE("api/articles/{id}")
    suspend fun deleteArticle(
        @Header("Authorization") token: String,
        @Path("id") articleId: String
    ): ApiResponse<Any>
}
