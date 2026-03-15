package com.example.marketlens

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide

class WatchlistFragment : Fragment() {

    private val viewModel: WatchlistViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_watchlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewWatchlist)
        val emptyState = view.findViewById<LinearLayout>(R.id.emptyState)
        val cacheAgeLabel = view.findViewById<TextView>(R.id.cacheAgeLabel)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.watchlistSwipeRefresh)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        swipeRefresh.setColorSchemeColors(android.graphics.Color.parseColor("#1DB954"))
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            swipeRefresh.isRefreshing = false
        }

        viewModel.enrichedWatchlist.observe(viewLifecycleOwner) { enrichedCoins ->
            if (enrichedCoins.isEmpty()) {
                emptyState.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                cacheAgeLabel.visibility = View.GONE
            } else {
                emptyState.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                val cacheAge = viewModel.getCacheAge()
                cacheAgeLabel.text = when {
                    cacheAge == null -> "Prices as saved"
                    cacheAge < 60_000 -> "Prices updated just now"
                    else -> "Prices updated ${cacheAge / 60_000}m ago"
                }
                cacheAgeLabel.visibility = View.VISIBLE

                recyclerView.adapter = WatchlistAdapter(enrichedCoins) { entity ->
                    val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                        putExtra("COIN_ID", entity.id)
                        putExtra("COIN_NAME", entity.name)
                        putExtra("COIN_PRICE", entity.currentPrice)
                        putExtra("COIN_CHANGE", entity.priceChange24h)
                        putExtra("COIN_IMAGE", entity.imageUrl)
                    }
                    startActivity(intent)
                }
            }
        }
    }
}

class WatchlistAdapter(
    private val coins: List<Pair<WatchlistEntity, Double?>>,
    private val onItemClick: (WatchlistEntity) -> Unit
) : RecyclerView.Adapter<WatchlistAdapter.WatchViewHolder>() {

    inner class WatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coinImage: ImageView = itemView.findViewById(R.id.coinImage)
        val coinName: TextView = itemView.findViewById(R.id.coinName)
        val coinSymbol: TextView = itemView.findViewById(R.id.coinSymbol)
        val coinPrice: TextView = itemView.findViewById(R.id.coinPrice)
        val coinChange: TextView = itemView.findViewById(R.id.coinChange)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_coin, parent, false)
        return WatchViewHolder(v)
    }

    override fun onBindViewHolder(holder: WatchViewHolder, position: Int) {
        val (coin, freshPrice) = coins[position]
        val displayPrice = freshPrice ?: coin.currentPrice
        holder.coinName.text = coin.name
        holder.coinSymbol.text = coin.symbol
        holder.coinPrice.text = "$${String.format("%,.2f", displayPrice)}"
        holder.coinChange.text = "${String.format("%.2f", coin.priceChange24h)}%"
        holder.coinChange.setTextColor(
            if (coin.priceChange24h >= 0) Color.parseColor("#1DB954")
            else Color.parseColor("#FF4444")
        )
        Glide.with(holder.itemView.context).load(coin.imageUrl).into(holder.coinImage)
        holder.itemView.setOnClickListener { onItemClick(coin) }
    }

    override fun getItemCount() = coins.size
}
