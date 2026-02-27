package com.example.marketlens

object AppCache {
    var coins: List<Coin> = emptyList()
    var lastCoinFetch: Long = 0
    val priceHistory: HashMap<String, Pair<PriceHistory, Long>> = HashMap()
    val cacheDuration = 120_000L // 2 minutes
}