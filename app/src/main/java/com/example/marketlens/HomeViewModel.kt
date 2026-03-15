package com.example.marketlens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class SortOrder { MARKET_CAP, PRICE_HIGH, PRICE_LOW, CHANGE_BEST, CHANGE_WORST }

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    sealed class UiState {
        object Loading : UiState()
        data class Success(
            val coins: List<Coin>,
            val currency: String,
            val isFromCache: Boolean,
            val cacheAgeMs: Long
        ) : UiState()
        data class Error(val message: String) : UiState()
    }

    data class GlobalMarketState(
        val marketCapUsd: Double = 0.0,
        val btcDominance: Double = 0.0,
        val change24h: Double = 0.0
    )

    private val prefs = application.getSharedPreferences("MarketLensPrefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private val _sortOrder = MutableStateFlow(SortOrder.MARKET_CAP)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val _globalMarket = MutableStateFlow<GlobalMarketState?>(null)
    val globalMarket: StateFlow<GlobalMarketState?> = _globalMarket

    init {
        AppCache.selectedCurrency = prefs.getString("selectedCurrency", "usd") ?: "usd"
        loadCoinsIfNeeded()
        loadGlobalMarket()
    }

    fun switchCurrency(currency: String) {
        if (AppCache.selectedCurrency == currency) return
        AppCache.selectedCurrency = currency
        prefs.edit().putString("selectedCurrency", currency).apply()

        val cached = AppCache.coinCache[currency]
        if (cached != null) {
            val age = System.currentTimeMillis() - cached.second
            if (age < AppCache.cacheDuration) {
                emitSuccess(cached.first, currency, isFromCache = true, cacheAgeMs = age)
                return
            }
        }
        loadCoinsIfNeeded()
    }

    fun refresh() {
        AppCache.coinCache.remove(AppCache.selectedCurrency)
        loadCoins(showLoadingOverlay = false)
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        val current = _uiState.value
        if (current is UiState.Success) {
            emitSuccess(current.coins, current.currency, current.isFromCache, current.cacheAgeMs)
        }
    }

    private fun loadCoinsIfNeeded() {
        val now = System.currentTimeMillis()
        val cached = AppCache.coinCache[AppCache.selectedCurrency]
        if (cached != null && (now - cached.second) < AppCache.cacheDuration) {
            emitSuccess(cached.first, AppCache.selectedCurrency, isFromCache = true, cacheAgeMs = now - cached.second)
            return
        }
        loadCoins()
    }

    private fun loadCoins(showLoadingOverlay: Boolean = true) {
        val cached = AppCache.coinCache[AppCache.selectedCurrency]
        if (cached != null) {
            val age = System.currentTimeMillis() - cached.second
            emitSuccess(cached.first, AppCache.selectedCurrency, isFromCache = true, cacheAgeMs = age)
        } else if (showLoadingOverlay) {
            _uiState.value = UiState.Loading
        }

        viewModelScope.launch {
            try {
                val coins = RetrofitClient.api.getCoins(currency = AppCache.selectedCurrency)
                AppCache.coinCache[AppCache.selectedCurrency] = Pair(coins, System.currentTimeMillis())
                emitSuccess(coins, AppCache.selectedCurrency, isFromCache = false, cacheAgeMs = 0)
            } catch (e: Exception) {
                val fallback = AppCache.coinCache[AppCache.selectedCurrency]
                if (fallback != null) {
                    val age = System.currentTimeMillis() - fallback.second
                    emitSuccess(fallback.first, AppCache.selectedCurrency, isFromCache = true, cacheAgeMs = age)
                } else {
                    _uiState.value = UiState.Error(e.message ?: "Failed to load coins")
                }
            }
        }
    }

    private fun loadGlobalMarket() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getGlobalMarket()
                val data = response.data
                _globalMarket.value = GlobalMarketState(
                    marketCapUsd = data.totalMarketCap["usd"] ?: 0.0,
                    btcDominance = data.marketCapPercentage["btc"] ?: 0.0,
                    change24h = data.marketCapChange24h
                )
            } catch (_: Exception) { /* non-critical */ }
        }
    }

    private fun sorted(coins: List<Coin>): List<Coin> = when (_sortOrder.value) {
        SortOrder.MARKET_CAP -> coins
        SortOrder.PRICE_HIGH -> coins.sortedByDescending { it.currentPrice }
        SortOrder.PRICE_LOW -> coins.sortedBy { it.currentPrice }
        SortOrder.CHANGE_BEST -> coins.sortedByDescending { it.priceChange24h }
        SortOrder.CHANGE_WORST -> coins.sortedBy { it.priceChange24h }
    }

    private fun emitSuccess(coins: List<Coin>, currency: String, isFromCache: Boolean, cacheAgeMs: Long) {
        _uiState.value = UiState.Success(sorted(coins), currency, isFromCache, cacheAgeMs)
    }
}
