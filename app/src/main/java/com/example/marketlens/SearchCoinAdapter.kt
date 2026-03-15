package com.example.marketlens

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SearchCoinAdapter(
    private var coins: List<SearchCoin>,
    private val onItemClick: (SearchCoin) -> Unit
) : RecyclerView.Adapter<SearchCoinAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coinImage: ImageView = itemView.findViewById(R.id.coinImage)
        val coinName: TextView = itemView.findViewById(R.id.coinName)
        val coinSymbol: TextView = itemView.findViewById(R.id.coinSymbol)
        val coinPrice: TextView = itemView.findViewById(R.id.coinPrice)
        val coinChange: TextView = itemView.findViewById(R.id.coinChange)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val coin = coins[position]
        holder.coinName.text = coin.name
        holder.coinSymbol.text = coin.symbol.uppercase()

        val cachedCoin = AppCache.coinCache[AppCache.selectedCurrency]?.first?.find { it.id == coin.id }
        if (cachedCoin != null) {
            val symbol = when (AppCache.selectedCurrency) { "inr" -> "₹"; "eur" -> "€"; else -> "$" }
            holder.coinPrice.text = "$symbol${String.format("%,.2f", cachedCoin.currentPrice)}"
            val change = cachedCoin.priceChange24h
            holder.coinChange.text = "${String.format("%.2f", change)}%"
            holder.coinChange.setTextColor(
                if (change >= 0) Color.parseColor("#1DB954") else Color.parseColor("#FF4444")
            )
        } else {
            holder.coinPrice.text = "Rank #${coin.market_cap_rank ?: "N/A"}"
            holder.coinChange.text = ""
        }

        Glide.with(holder.itemView.context)
            .load(coin.thumb)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .circleCrop()
            .into(holder.coinImage)

        holder.itemView.setOnClickListener { onItemClick(coin) }
    }

    override fun getItemCount() = coins.size

    fun updateCoins(newCoins: List<SearchCoin>) {
        coins = newCoins
        notifyDataSetChanged()
    }
}
