package com.example.marketlens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CoinAdapter(
    private var coins: List<Coin>,
    private var currencySymbol: String = "$",
    private val onItemClick: (Coin, ImageView) -> Unit
) : RecyclerView.Adapter<CoinAdapter.CoinViewHolder>() {

    inner class CoinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coinImage: ImageView = itemView.findViewById(R.id.coinImage)
        val coinName: TextView = itemView.findViewById(R.id.coinName)
        val coinSymbol: TextView = itemView.findViewById(R.id.coinSymbol)
        val coinPrice: TextView = itemView.findViewById(R.id.coinPrice)
        val coinChange: TextView = itemView.findViewById(R.id.coinChange)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coin, parent, false)
        return CoinViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val coin = coins[position]
        holder.coinName.text = coin.name
        holder.coinSymbol.text = coin.symbol
        val priceText = "$currencySymbol${String.format("%,.2f", coin.currentPrice)}"
        holder.coinPrice.text = priceText

        val change = coin.priceChange24h
        holder.coinChange.text = "${String.format("%.2f", change)}%"
        holder.coinChange.setTextColor(
            if (change >= 0) Color.parseColor("#1DB954") else Color.parseColor("#FF4444")
        )

        holder.coinImage.transitionName = "coin_image_${coin.id}"

        Glide.with(holder.itemView.context)
            .load(coin.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .circleCrop()
            .into(holder.coinImage)

        holder.itemView.setOnClickListener { onItemClick(coin, holder.coinImage) }

        holder.itemView.setOnLongClickListener {
            val clipboard = it.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("price", priceText))
            Toast.makeText(it.context, "${coin.name} price copied", Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun getItemCount() = coins.size

    fun updateCoins(newCoins: List<Coin>) {
        coins = newCoins
        notifyDataSetChanged()
    }

    fun updateCurrencySymbol(symbol: String) {
        currencySymbol = symbol
        notifyDataSetChanged()
    }
}
