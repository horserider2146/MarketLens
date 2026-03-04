package com.example.marketlens

object AppCache {
    var coins: List<Coin> = emptyList()
    var lastCoinFetch: Long = 0
    var selectedCurrency: String = "usd"
    val coinCache: HashMap<String, Pair<List<Coin>, Long>> = HashMap()
    val priceHistory: HashMap<String, Pair<PriceHistory, Long>> = HashMap()
    val cacheDuration = 120_000L
}