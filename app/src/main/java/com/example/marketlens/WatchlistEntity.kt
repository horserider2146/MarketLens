package com.example.marketlens

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val symbol: String,
    val imageUrl: String,
    val currentPrice: Double,
    val priceChange24h: Double
)