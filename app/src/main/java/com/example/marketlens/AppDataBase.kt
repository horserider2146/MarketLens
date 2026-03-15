package com.example.marketlens

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WatchlistEntity::class, PortfolioEntity::class, AlertEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun watchlistDao(): WatchlistDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun alertDao(): AlertDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `portfolio` (
                        `coinId` TEXT NOT NULL, `coinName` TEXT NOT NULL,
                        `symbol` TEXT NOT NULL, `imageUrl` TEXT NOT NULL,
                        `amount` REAL NOT NULL, `buyPrice` REAL NOT NULL,
                        `currency` TEXT NOT NULL DEFAULT 'usd',
                        PRIMARY KEY(`coinId`))"""
                )
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `price_alerts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `coinId` TEXT NOT NULL, `coinName` TEXT NOT NULL,
                        `targetPrice` REAL NOT NULL, `isAbove` INTEGER NOT NULL,
                        `currency` TEXT NOT NULL DEFAULT 'usd')"""
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "marketlens_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
