package com.example.marketlens

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist")
    fun getAllWatchlist(): LiveData<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(coin: WatchlistEntity)

    @Delete
    suspend fun removeFromWatchlist(coin: WatchlistEntity)

    @Query("SELECT * FROM watchlist WHERE id = :coinId")
    suspend fun getCoinById(coinId: String): WatchlistEntity?
}