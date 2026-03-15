package com.example.marketlens

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var adapter: CoinAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private val currencySymbols = mapOf("usd" to "$", "inr" to "₹", "eur" to "€")
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewCoins)
        val btnUSD = view.findViewById<Button>(R.id.btnUSD)
        val btnINR = view.findViewById<Button>(R.id.btnINR)
        val btnEUR = view.findViewById<Button>(R.id.btnEUR)
        val btnSort = view.findViewById<ImageButton>(R.id.btnSort)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val offlineBanner = view.findViewById<TextView>(R.id.offlineBanner)
        val globalMarketBar = view.findViewById<TextView>(R.id.globalMarketBar)
        val shimmerLayout = view.findViewById<ShimmerFrameLayout>(R.id.shimmerLayout)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)

        swipeRefresh.setColorSchemeColors(Color.parseColor("#1DB954"))

        adapter = CoinAdapter(emptyList(), "$") { coin, imageView ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("COIN_ID", coin.id)
                putExtra("COIN_NAME", coin.name)
                putExtra("COIN_PRICE", coin.currentPrice)
                putExtra("COIN_CHANGE", coin.priceChange24h)
                putExtra("COIN_IMAGE", coin.imageUrl)
                putExtra("COIN_CURRENCY", AppCache.selectedCurrency)
                putExtra("COIN_SYMBOL", currencySymbols[AppCache.selectedCurrency] ?: "$")
            }
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(), imageView, "coin_image_${coin.id}"
            )
            startActivity(intent, options.toBundle())
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnUSD.setOnClickListener { viewModel.switchCurrency("usd") }
        btnINR.setOnClickListener { viewModel.switchCurrency("inr") }
        btnEUR.setOnClickListener { viewModel.switchCurrency("eur") }
        swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        btnSort.setOnClickListener { showSortMenu(btnSort) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is HomeViewModel.UiState.Loading -> {
                                progressBar.visibility = View.GONE
                                shimmerLayout.visibility = View.VISIBLE
                                shimmerLayout.startShimmer()
                                recyclerView.visibility = View.GONE
                                offlineBanner.visibility = View.GONE
                                swipeRefresh.isRefreshing = false
                            }
                            is HomeViewModel.UiState.Success -> {
                                shimmerLayout.stopShimmer()
                                shimmerLayout.visibility = View.GONE
                                progressBar.visibility = View.GONE
                                recyclerView.visibility = View.VISIBLE
                                swipeRefresh.isRefreshing = false
                                adapter.updateCoins(state.coins)
                                adapter.updateCurrencySymbol(currencySymbols[state.currency] ?: "$")
                                updateButtonStates(btnUSD, btnINR, btnEUR, state.currency)
                                if (state.isFromCache && state.cacheAgeMs > 60_000) {
                                    val min = state.cacheAgeMs / 60_000
                                    offlineBanner.text = "Cached data · updated ${min}m ago"
                                    offlineBanner.visibility = View.VISIBLE
                                } else {
                                    offlineBanner.visibility = View.GONE
                                }
                            }
                            is HomeViewModel.UiState.Error -> {
                                shimmerLayout.stopShimmer()
                                shimmerLayout.visibility = View.GONE
                                progressBar.visibility = View.GONE
                                recyclerView.visibility = View.VISIBLE
                                swipeRefresh.isRefreshing = false
                                offlineBanner.visibility = View.GONE
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                launch {
                    viewModel.globalMarket.collect { gm ->
                        if (gm != null) {
                            val cap = formatMarketCap(gm.marketCapUsd)
                            val changeSign = if (gm.change24h >= 0) "+" else ""
                            val changeColor = if (gm.change24h >= 0) "#1DB954" else "#FF4444"
                            globalMarketBar.text =
                                "MCap: $cap  |  BTC Dom: ${"%.1f".format(gm.btcDominance)}%  |  24h: $changeSign${"%.2f".format(gm.change24h)}%"
                            globalMarketBar.setTextColor(Color.parseColor(changeColor))
                            globalMarketBar.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun showSortMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.apply {
            add(0, 0, 0, "Market Cap (Default)")
            add(0, 1, 1, "Price: High → Low")
            add(0, 2, 2, "Price: Low → High")
            add(0, 3, 3, "24h Change: Best")
            add(0, 4, 4, "24h Change: Worst")
        }
        popup.setOnMenuItemClickListener { item ->
            viewModel.setSortOrder(
                when (item.itemId) {
                    1 -> SortOrder.PRICE_HIGH
                    2 -> SortOrder.PRICE_LOW
                    3 -> SortOrder.CHANGE_BEST
                    4 -> SortOrder.CHANGE_WORST
                    else -> SortOrder.MARKET_CAP
                }
            )
            true
        }
        popup.show()
    }

    private fun updateButtonStates(btnUSD: Button, btnINR: Button, btnEUR: Button, currency: String) {
        val active = Color.parseColor("#1DB954")
        val inactive = Color.parseColor("#333333")
        btnUSD.backgroundTintList = android.content.res.ColorStateList.valueOf(if (currency == "usd") active else inactive)
        btnINR.backgroundTintList = android.content.res.ColorStateList.valueOf(if (currency == "inr") active else inactive)
        btnEUR.backgroundTintList = android.content.res.ColorStateList.valueOf(if (currency == "eur") active else inactive)
    }

    private fun formatMarketCap(value: Double): String = when {
        value >= 1_000_000_000_000 -> "${"%.2f".format(value / 1_000_000_000_000)}T"
        value >= 1_000_000_000 -> "${"%.1f".format(value / 1_000_000_000)}B"
        else -> "${"%.0f".format(value / 1_000_000)}M"
    }
}
