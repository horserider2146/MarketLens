package com.example.marketlens

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PortfolioAdapter(
    private var items: List<PortfolioHolding>,
    private val onLongClick: (PortfolioEntity) -> Unit
) : RecyclerView.Adapter<PortfolioAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.portfolioItemImage)
        val name: TextView = view.findViewById(R.id.portfolioItemName)
        val amount: TextView = view.findViewById(R.id.portfolioItemAmount)
        val value: TextView = view.findViewById(R.id.portfolioItemValue)
        val pl: TextView = view.findViewById(R.id.portfolioItemPL)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_portfolio, parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val holding = items[position]
        val entity = holding.entity
        val currency = AppCache.selectedCurrency
        val symbol = when (currency) { "inr" -> "₹"; "eur" -> "€"; else -> "$" }

        Glide.with(holder.itemView.context)
            .load(entity.imageUrl)
            .placeholder(R.drawable.ic_coin_placeholder)
            .error(R.drawable.ic_coin_placeholder)
            .circleCrop()
            .into(holder.image)

        holder.name.text = entity.coinName
        holder.amount.text = "${String.format("%.4f", entity.amount)} ${entity.symbol.uppercase()}"

        val displayValue = holding.currentValue ?: (entity.amount * entity.buyPrice)
        holder.value.text = "$symbol${String.format("%,.2f", displayValue)}"

        val pl = holding.profitLoss
        val plPct = holding.profitLossPct
        if (pl != null && plPct != null) {
            val sign = if (pl >= 0) "+" else ""
            holder.pl.text = "$sign$symbol${String.format("%,.2f", pl)} ($sign${String.format("%.1f", plPct)}%)"
            holder.pl.setTextColor(if (pl >= 0) Color.parseColor("#1DB954") else Color.parseColor("#FF4444"))
        } else {
            holder.pl.text = "Price unavailable"
            holder.pl.setTextColor(Color.GRAY)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(entity)
            true
        }
    }

    fun update(newItems: List<PortfolioHolding>) {
        items = newItems
        notifyDataSetChanged()
    }
}
