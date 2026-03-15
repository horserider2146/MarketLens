package com.example.marketlens

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PortfolioFragment : Fragment() {

    private val viewModel: PortfolioViewModel by viewModels()
    private lateinit var adapter: PortfolioAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_portfolio, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.portfolioRecycler)
        val emptyView = view.findViewById<TextView>(R.id.portfolioEmpty)
        val totalValueView = view.findViewById<TextView>(R.id.portfolioTotalValue)
        val totalCostView = view.findViewById<TextView>(R.id.portfolioTotalCost)
        val totalPLView = view.findViewById<TextView>(R.id.portfolioTotalPL)

        adapter = PortfolioAdapter(emptyList()) { entity ->
            AlertDialog.Builder(requireContext())
                .setTitle("Remove ${entity.coinName}?")
                .setMessage("Remove this holding from your portfolio?")
                .setPositiveButton("Remove") { _, _ -> viewModel.deleteHolding(entity) }
                .setNegativeButton("Cancel", null)
                .show()
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        val currency = AppCache.selectedCurrency
        val symbol = when (currency) { "inr" -> "₹"; "eur" -> "€"; else -> "$" }

        viewModel.holdings.observe(viewLifecycleOwner) { holdings ->
            adapter.update(holdings)
            emptyView.visibility = if (holdings.isEmpty()) View.VISIBLE else View.GONE
            recycler.visibility = if (holdings.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.totalValue.observe(viewLifecycleOwner) { total ->
            totalValueView.text = "$symbol${String.format("%,.2f", total)}"
        }

        viewModel.totalCost.observe(viewLifecycleOwner) { cost ->
            totalCostView.text = "$symbol${String.format("%,.2f", cost)}"
            val currentTotal = viewModel.totalValue.value ?: cost
            val pl = currentTotal - cost
            val plPct = if (cost > 0) (pl / cost) * 100 else 0.0
            val sign = if (pl >= 0) "+" else ""
            totalPLView.text = "$sign$symbol${String.format("%,.2f", pl)} ($sign${String.format("%.1f", plPct)}%)"
            totalPLView.setTextColor(if (pl >= 0) Color.parseColor("#1DB954") else Color.parseColor("#FF4444"))
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }
}
