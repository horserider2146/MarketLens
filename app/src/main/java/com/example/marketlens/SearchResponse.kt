package com.example.marketlens

data class SearchResponse(
    val coins: List<SearchCoin>
)

data class SearchCoin(
    val id: String,
    val name: String,
    val symbol: String,
    val thumb: String,
    val market_cap_rank: Int?
)