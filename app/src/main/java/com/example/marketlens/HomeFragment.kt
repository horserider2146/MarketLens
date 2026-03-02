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
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var adapter: CoinAdapter
    private var selectedCurrency = "usd"
    private val currencySymbols = mapOf("usd" to "$", "inr" to "₹", "eur" to "€")
    private val coinCache = mutableMapOf<String, Pair<List<Coin>, Long>>()
    private val cacheDuration = 120_000L

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

        adapter = CoinAdapter(emptyList(), "$") { coin ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("COIN_ID", coin.id)
                putExtra("COIN_NAME", coin.name)
                putExtra("COIN_PRICE", coin.currentPrice)
                putExtra("COIN_CHANGE", coin.priceChange24h)
                putExtra("COIN_IMAGE", coin.imageUrl)
                putExtra("COIN_CURRENCY", selectedCurrency)
                putExtra("COIN_SYMBOL", currencySymbols[selectedCurrency] ?: "$")
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnUSD.setOnClickListener { switchCurrency("usd", btnUSD, btnINR, btnEUR) }
        btnINR.setOnClickListener { switchCurrency("inr", btnINR, btnUSD, btnEUR) }
        btnEUR.setOnClickListener { switchCurrency("eur", btnEUR, btnUSD, btnINR) }

        loadCoins()
    }

    private fun switchCurrency(currency: String, active: Button, vararg inactive: Button) {
        if (selectedCurrency == currency) return // don't reload if same currency

        selectedCurrency = currency
        active.backgroundTintList = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor("#1DB954"))
        inactive.forEach {
            it.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#333333"))
        }

        // Update symbol on existing list immediately
        adapter.updateCurrencySymbol(currencySymbols[currency] ?: "$")

        loadCoins()
    }

    private fun loadCoins() {
        val now = System.currentTimeMillis()
        val cached = coinCache[selectedCurrency]

        // Use per-currency cache
        if (cached != null && (now - cached.second) < cacheDuration) {
            adapter.updateCoins(cached.first)
            adapter.updateCurrencySymbol(currencySymbols[selectedCurrency] ?: "$")
            return
        }

        val progressBar = view?.findViewById<android.widget.ProgressBar>(R.id.progressBar)
        progressBar?.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val coins = RetrofitClient.api.getCoins(currency = selectedCurrency)
                coinCache[selectedCurrency] = Pair(coins, System.currentTimeMillis())
                adapter.updateCoins(coins)
                adapter.updateCurrencySymbol(currencySymbols[selectedCurrency] ?: "$")
            } catch (e: Exception) {
                val cachedFallback = coinCache[selectedCurrency]
                if (cachedFallback != null) {
                    adapter.updateCoins(cachedFallback.first)
                    adapter.updateCurrencySymbol(currencySymbols[selectedCurrency] ?: "$")
                    Toast.makeText(requireContext(), "Showing cached data", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Please wait before switching again", Toast.LENGTH_SHORT).show()
                }
            } finally {
                progressBar?.visibility = View.GONE
            }
        }
    }
}