package com.example.marketlens

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class WatchlistFragment : Fragment() {

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
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val db = AppDatabase.getDatabase(requireContext())

        db.watchlistDao().getAllWatchlist().observe(viewLifecycleOwner) { coins ->
            val emptyState = view.findViewById<LinearLayout>(R.id.emptyState)
            if (coins.isEmpty()) {
                emptyState.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyState.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
            recyclerView.adapter = WatchlistAdapter(coins) { coin ->
                val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                    putExtra("COIN_ID", coin.id)
                    putExtra("COIN_NAME", coin.name)
                    putExtra("COIN_PRICE", coin.currentPrice)
                    putExtra("COIN_CHANGE", coin.priceChange24h)
                    putExtra("COIN_IMAGE", coin.imageUrl)
                }
                startActivity(intent)
            }
        }
    }
}

class WatchlistAdapter(
    private val coins: List<WatchlistEntity>,
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
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coin, parent, false)
        return WatchViewHolder(v)
    }

    override fun onBindViewHolder(holder: WatchViewHolder, position: Int) {
        val coin = coins[position]
        holder.coinName.text = coin.name
        holder.coinSymbol.text = coin.symbol
        holder.coinPrice.text = "$${String.format("%,.2f", coin.currentPrice)}"
        holder.coinChange.text = "${String.format("%.2f", coin.priceChange24h)}%"
        holder.coinChange.setTextColor(
            if (coin.priceChange24h >= 0) Color.parseColor("#1DB954")
            else Color.parseColor("#FF4444")
        )
        Glide.with(holder.itemView.context)
            .load(coin.imageUrl)
            .into(holder.coinImage)

        holder.itemView.setOnClickListener { onItemClick(coin) }
    }

    override fun getItemCount() = coins.size
}