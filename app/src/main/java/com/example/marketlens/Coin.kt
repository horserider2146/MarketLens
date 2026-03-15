package com.example.marketlens

import com.google.gson.annotations.SerializedName

data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    @SerializedName("current_price") val currentPrice: Double,
    @SerializedName("price_change_percentage_24h") val priceChange24h: Double,
    @SerializedName("market_cap") val marketCap: Double,
    @SerializedName("total_volume") val totalVolume: Double,
    @SerializedName("image") val imageUrl: String,
    @SerializedName("market_cap_rank") val marketCapRank: Int? = null,
    @SerializedName("circulating_supply") val circulatingSupply: Double? = null,
    @SerializedName("ath") val ath: Double? = null,
    @SerializedName("atl") val atl: Double? = null
)