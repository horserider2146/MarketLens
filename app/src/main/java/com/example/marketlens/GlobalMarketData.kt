package com.example.marketlens

import com.google.gson.annotations.SerializedName

data class GlobalMarketResponse(val data: GlobalMarketData)

data class GlobalMarketData(
    @SerializedName("total_market_cap") val totalMarketCap: Map<String, Double>,
    @SerializedName("market_cap_percentage") val marketCapPercentage: Map<String, Double>,
    @SerializedName("market_cap_change_percentage_24h_usd") val marketCapChange24h: Double
)
