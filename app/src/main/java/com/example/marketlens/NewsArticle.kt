package com.example.marketlens

data class NewsResponse(
    val status: String,
    val results: List<NewsArticle>
)

data class NewsArticle(
    val title: String,
    val description: String?,
    val link: String,
    val source_name: String,
    val pubDate: String,
    val image_url: String?
)