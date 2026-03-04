package com.example.marketlens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var adapter: CoinAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private val currencySymbols = mapOf("usd" to "$", "inr" to "₹", "eur" to "€")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewCoins)
        val btnUSD = view.findViewById<Button>(R.id.btnUSD)
        val btnINR = view.findViewById<Button>(R.id.btnINR)
        val btnEUR = view.findViewById<Button>(R.id.btnEUR)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)

        // Green spinner color
        swipeRefresh.setColorSchemeColors(android.graphics.Color.parseColor("#1DB954"))

        adapter = CoinAdapter(
            emptyList(),
            currencySymbols[AppCache.selectedCurrency] ?: "$"
        ) { coin ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("COIN_ID", coin.id)
                putExtra("COIN_NAME", coin.name)
                putExtra("COIN_PRICE", coin.currentPrice)
                putExtra("COIN_CHANGE", coin.priceChange24h)
                putExtra("COIN_IMAGE", coin.imageUrl)
                putExtra("COIN_CURRENCY", AppCache.selectedCurrency)
                putExtra("COIN_SYMBOL", currencySymbols[AppCache.selectedCurrency] ?: "$")
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        updateButtonStates(btnUSD, btnINR, btnEUR)

        // Show cached data immediately
        val cached = AppCache.coinCache[AppCache.selectedCurrency]
        if (cached != null) {
            adapter.updateCoins(cached.first)
            adapter.updateCurrencySymbol(currencySymbols[AppCache.selectedCurrency] ?: "$")
        }

        btnUSD.setOnClickListener { switchCurrency("usd", btnUSD, btnINR, btnEUR) }
        btnINR.setOnClickListener { switchCurrency("inr", btnINR, btnUSD, btnEUR) }
        btnEUR.setOnClickListener { switchCurrency("eur", btnEUR, btnUSD, btnINR) }

        // Pull to refresh — force fresh data
        swipeRefresh.setOnRefreshListener {
            AppCache.coinCache.remove(AppCache.selectedCurrency)
            loadCoins(forceRefresh = true)
        }

        loadCoinsIfNeeded()
    }

    private fun updateButtonStates(btnUSD: Button, btnINR: Button, btnEUR: Button) {
        val active = android.graphics.Color.parseColor("#1DB954")
        val inactive = android.graphics.Color.parseColor("#333333")
        btnUSD.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (AppCache.selectedCurrency == "usd") active else inactive)
        btnINR.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (AppCache.selectedCurrency == "inr") active else inactive)
        btnEUR.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (AppCache.selectedCurrency == "eur") active else inactive)
    }

    private fun switchCurrency(currency: String, active: Button, vararg inactive: Button) {
        if (AppCache.selectedCurrency == currency) return

        AppCache.selectedCurrency = currency
        active.backgroundTintList = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor("#1DB954"))
        inactive.forEach {
            it.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#333333"))
        }

        val cached = AppCache.coinCache[currency]
        if (cached != null) {
            adapter.updateCoins(cached.first)
            adapter.updateCurrencySymbol(currencySymbols[currency] ?: "$")
            return
        }

        loadCoinsIfNeeded()
    }

    private fun loadCoinsIfNeeded() {
        val now = System.currentTimeMillis()
        val cached = AppCache.coinCache[AppCache.selectedCurrency]

        if (cached != null && (now - cached.second) < AppCache.cacheDuration) {
            adapter.updateCoins(cached.first)
            adapter.updateCurrencySymbol(currencySymbols[AppCache.selectedCurrency] ?: "$")
            return
        }

        loadCoins()
    }

    private fun loadCoins(forceRefresh: Boolean = false) {
        val progressBar = view?.findViewById<android.widget.ProgressBar>(R.id.progressBar)
        if (!forceRefresh) progressBar?.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val coins = RetrofitClient.api.getCoins(currency = AppCache.selectedCurrency)
                AppCache.coinCache[AppCache.selectedCurrency] = Pair(coins, System.currentTimeMillis())
                adapter.updateCoins(coins)
                adapter.updateCurrencySymbol(currencySymbols[AppCache.selectedCurrency] ?: "$")
            } catch (e: Exception) {
                val cachedFallback = AppCache.coinCache[AppCache.selectedCurrency]
                if (cachedFallback != null) {
                    adapter.updateCoins(cachedFallback.first)
                    adapter.updateCurrencySymbol(currencySymbols[AppCache.selectedCurrency] ?: "$")
                } else {
                    Toast.makeText(requireContext(), "Please wait a moment", Toast.LENGTH_SHORT).show()
                }
            } finally {
                progressBar?.visibility = View.GONE
                swipeRefresh.isRefreshing = false
            }
        }
    }
}