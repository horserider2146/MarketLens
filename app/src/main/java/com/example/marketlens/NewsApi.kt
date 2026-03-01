package com.example.marketlens

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("news")
    suspend fun getCryptoNews(
        @Query("apikey") apiKey: String,
        @Query("q") query: String = "cryptocurrency",
        @Query("language") language: String = "en"
    ): NewsResponse
}