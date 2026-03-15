package com.example.marketlens

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio")
data class PortfolioEntity(
    @PrimaryKey val coinId: String,
    val coinName: String,
    val symbol: String,
    val imageUrl: String,
    val amount: Double,
    val buyPrice: Double,
    val currency: String = "usd"
)
