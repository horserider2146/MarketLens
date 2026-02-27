package com.example.marketlens

import com.google.gson.annotations.SerializedName

data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    @SerializedName("current_price") val currentPrice: Double,
    @SerializedName("price_change_percentage_24h") val priceChange24h: Double,
    @SerializedName("market_cap") val marketCap: Long,
    @SerializedName("total_volume") val totalVolume: Long,
    @SerializedName("image") val imageUrl: String
)