package com.example.marketlens

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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val coin = coins[position]
        holder.coinName.text = coin.name
        holder.coinSymbol.text = coin.symbol
        holder.coinPrice.text = "Rank #${coin.market_cap_rank ?: "N/A"}"
        holder.coinChange.text = ""

        Glide.with(holder.itemView.context)
            .load(coin.thumb)
            .into(holder.coinImage)

        holder.itemView.setOnClickListener { onItemClick(coin) }
    }

    override fun getItemCount() = coins.size

    fun updateCoins(newCoins: List<SearchCoin>) {
        coins = newCoins
        notifyDataSetChanged()
    }
}