package com.example.marketlens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

class WatchlistViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _dbSource = db.watchlistDao().getAllWatchlist()
    private val _refreshTrigger = MutableLiveData(Unit)

    val enrichedWatchlist = MediatorLiveData<List<Pair<WatchlistEntity, Double?>>>().apply {
        addSource(_dbSource) { _ -> value = buildEnriched() }
        addSource(_refreshTrigger) { _ -> value = buildEnriched() }
    }

    private fun buildEnriched(): List<Pair<WatchlistEntity, Double?>> {
        val coins = _dbSource.value ?: return emptyList()
        val cachedCoins = AppCache.coinCache[AppCache.selectedCurrency]?.first
        return coins.map { entity ->
            val freshPrice = cachedCoins?.find { it.id == entity.id }?.currentPrice
            Pair(entity, freshPrice)
        }
    }

    fun refresh() {
        _refreshTrigger.value = Unit
    }

    fun getCacheAge(): Long? {
        val entry = AppCache.coinCache[AppCache.selectedCurrency] ?: return null
        return System.currentTimeMillis() - entry.second
    }
}
