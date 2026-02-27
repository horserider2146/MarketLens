package com.example.marketlens

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CoinAdapter(
    private var coins: List<Coin>,
    private val onItemClick: (Coin) -> Unit
) : RecyclerView.Adapter<CoinAdapter.CoinViewHolder>() {

    inner class CoinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coinImage: ImageView = itemView.findViewById(R.id.coinImage)
        val coinName: TextView = itemView.findViewById(R.id.coinName)
        val coinSymbol: TextView = itemView.findViewById(R.id.coinSymbol)
        val coinPrice: TextView = itemView.findViewById(R.id.coinPrice)
        val coinChange: TextView = itemView.findViewById(R.id.coinChange)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coin, parent, false)
        return CoinViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val coin = coins[position]

        holder.coinName.text = coin.name
        holder.coinSymbol.text = coin.symbol
        holder.coinPrice.text = "$${String.format("%,.2f", coin.currentPrice)}"

        val change = coin.priceChange24h
        holder.coinChange.text = "${String.format("%.2f", change)}%"
        holder.coinChange.setTextColor(
            if (change >= 0) Color.parseColor("#1DB954")
            else Color.parseColor("#FF4444")
        )

        Glide.with(holder.itemView.context)
            .load(coin.imageUrl)
            .into(holder.coinImage)

        holder.itemView.setOnClickListener { onItemClick(coin) }
    }

    override fun getItemCount() = coins.size

    fun updateCoins(newCoins: List<Coin>) {
        coins = newCoins
        notifyDataSetChanged()
    }
}