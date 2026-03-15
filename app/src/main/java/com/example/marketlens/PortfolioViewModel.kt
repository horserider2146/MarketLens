package com.example.marketlens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class PortfolioHolding(
    val entity: PortfolioEntity,
    val currentPrice: Double?,
    val currentValue: Double?,
    val profitLoss: Double?,
    val profitLossPct: Double?
)

class PortfolioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _rawHoldings: LiveData<List<PortfolioEntity>> = db.portfolioDao().getAllHoldings()

    val holdings = MediatorLiveData<List<PortfolioHolding>>().apply {
        addSource(_rawHoldings) { entities ->
            value = enrich(entities)
        }
    }

    val totalValue = MediatorLiveData<Double>().apply {
        addSource(holdings) { list ->
            value = list.sumOf { it.currentValue ?: (it.entity.amount * it.entity.buyPrice) }
        }
    }

    val totalCost = MediatorLiveData<Double>().apply {
        addSource(_rawHoldings) { entities ->
            value = entities.sumOf { it.amount * it.buyPrice }
        }
    }

    private fun enrich(entities: List<PortfolioEntity>): List<PortfolioHolding> {
        val currency = AppCache.selectedCurrency
        val cachedCoins = AppCache.coinCache[currency]?.first
        return entities.map { entity ->
            val coin = cachedCoins?.find { it.id == entity.coinId }
            val currentPrice = coin?.currentPrice
            val currentValue = currentPrice?.let { it * entity.amount }
            val costBasis = entity.buyPrice * entity.amount
            val pl = currentValue?.let { it - costBasis }
            val plPct = if (entity.buyPrice > 0) pl?.let { (it / costBasis) * 100 } else null
            PortfolioHolding(entity, currentPrice, currentValue, pl, plPct)
        }
    }

    fun refresh() {
        val entities = _rawHoldings.value ?: return
        holdings.value = enrich(entities)
    }

    fun deleteHolding(entity: PortfolioEntity) {
        viewModelScope.launch {
            db.portfolioDao().deleteHolding(entity)
        }
    }
}
