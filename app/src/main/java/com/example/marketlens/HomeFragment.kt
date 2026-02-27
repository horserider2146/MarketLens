package com.example.marketlens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var adapter: CoinAdapter
    private var cachedCoins: List<Coin> = emptyList()
    private var lastFetchTime: Long = 0
    private val cacheDuration = 60_000L // 60 seconds

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
        adapter = CoinAdapter(emptyList()) { coin ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("COIN_ID", coin.id)
                putExtra("COIN_NAME", coin.name)
                putExtra("COIN_PRICE", coin.currentPrice)
                putExtra("COIN_CHANGE", coin.priceChange24h)
                putExtra("COIN_IMAGE", coin.imageUrl)
            }
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadCoins()
    }

    private fun loadCoins() {
        val now = System.currentTimeMillis()

        if (AppCache.coins.isNotEmpty() && (now - AppCache.lastCoinFetch) < AppCache.cacheDuration) {
            adapter.updateCoins(AppCache.coins)
            return
        }

        val progressBar = view?.findViewById<android.widget.ProgressBar>(R.id.progressBar)
        progressBar?.visibility = android.view.View.VISIBLE

        lifecycleScope.launch {
            try {
                val coins = RetrofitClient.api.getCoins()
                AppCache.coins = coins
                AppCache.lastCoinFetch = System.currentTimeMillis()
                adapter.updateCoins(coins)
            } catch (e: Exception) {
                if (AppCache.coins.isNotEmpty()) {
                    adapter.updateCoins(AppCache.coins)
                } else {
                    Toast.makeText(requireContext(), "Too many requests. Wait a moment.", Toast.LENGTH_LONG).show()
                }
            } finally {
                progressBar?.visibility = android.view.View.GONE
            }
        }
    }
}