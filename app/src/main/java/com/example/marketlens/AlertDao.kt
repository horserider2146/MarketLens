package com.example.marketlens

import androidx.room.*

@Dao
interface AlertDao {
    @Query("SELECT * FROM price_alerts")
    suspend fun getAllAlerts(): List<AlertEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Delete
    suspend fun deleteAlert(alert: AlertEntity)

    @Query("SELECT * FROM price_alerts WHERE coinId = :coinId")
    suspend fun getAlertsForCoin(coinId: String): List<AlertEntity>
}
