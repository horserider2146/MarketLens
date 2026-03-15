package com.example.marketlens

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio")
    fun getAllHoldings(): LiveData<List<PortfolioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHolding(holding: PortfolioEntity)

    @Delete
    suspend fun deleteHolding(holding: PortfolioEntity)

    @Query("SELECT * FROM portfolio WHERE coinId = :coinId")
    suspend fun getHoldingById(coinId: String): PortfolioEntity?
}
