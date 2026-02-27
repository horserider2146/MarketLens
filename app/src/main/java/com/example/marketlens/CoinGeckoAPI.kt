package com.example.marketlens

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinGeckoApi {

    @GET("coins/markets")
    suspend fun getCoins(
        @Query("vs_currency") currency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): List<Coin>

    @GET("coins/{id}/market_chart")
    suspend fun getCoinPriceHistory(
        @Path("id") id: String,
        @Query("vs_currency") currency: String = "usd",
        @Query("days") days: Int = 30
    ): PriceHistory

    @GET("search")
    suspend fun searchCoins(
        @Query("query") query: String
    ): SearchResponse
}