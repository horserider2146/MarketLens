package com.example.marketlens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var currentCoin: WatchlistEntity? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) showAlertDialog()
        else Toast.makeText(this, "Notification permission required for alerts", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        db = AppDatabase.getDatabase(this)

        val coinId = intent.getStringExtra("COIN_ID") ?: return
        val coinName = intent.getStringExtra("COIN_NAME") ?: ""
        val coinPrice = intent.getDoubleExtra("COIN_PRICE", 0.0)
        val coinChange = intent.getDoubleExtra("COIN_CHANGE", 0.0)
        val coinImage = intent.getStringExtra("COIN_IMAGE") ?: ""
        val currency = AppCache.selectedCurrency

        currentCoin = WatchlistEntity(
            id = coinId,
            name = coinName,
            symbol = "",
            imageUrl = coinImage,
            currentPrice = coinPrice,
            priceChange24h = coinChange
        )

        // Coin image with shared element transition
        val imageView = findViewById<ImageView>(R.id.detailCoinImage)
        imageView.transitionName = "coin_image_$coinId"
        Glide.with(this)
            .load(coinImage)
            .placeholder(R.drawable.ic_coin_placeholder)
            .error(R.drawable.ic_coin_placeholder)
            .circleCrop()
            .into(imageView)

        // Header text
        val currencySymbol = when (currency) {
            "inr" -> "₹"
            "eur" -> "€"
            else -> "$"
        }
        findViewById<TextView>(R.id.detailName).text = coinName
        findViewById<TextView>(R.id.detailPrice).text = "$currencySymbol${String.format("%,.2f", coinPrice)}"

        val changeView = findViewById<TextView>(R.id.detailChange)
        val sign = if (coinChange >= 0) "+" else ""
        changeView.text = "$sign${String.format("%.2f", coinChange)}%"
        changeView.setTextColor(
            if (coinChange >= 0) Color.parseColor("#1DB954")
            else Color.parseColor("#FF4444")
        )

        // Market stats from cache
        val cachedCoin = AppCache.coinCache[currency]?.first?.find { it.id == coinId }
        if (cachedCoin != null) {
            val statsCard = findViewById<LinearLayout>(R.id.statsCard)
            statsCard.visibility = View.VISIBLE
            findViewById<TextView>(R.id.statRank).text = cachedCoin.marketCapRank?.let { "#$it" } ?: "–"
            findViewById<TextView>(R.id.statMarketCap).text = formatLargeNumber(cachedCoin.marketCap, currencySymbol)
            findViewById<TextView>(R.id.statSupply).text = formatLargeNumber(cachedCoin.circulatingSupply ?: 0.0, "")
            findViewById<TextView>(R.id.statAth).text = cachedCoin.ath?.let { "$currencySymbol${String.format("%,.2f", it)}" } ?: "–"
        }

        // Watchlist button
        val watchlistBtn = findViewById<Button>(R.id.watchlistBtn)
        lifecycleScope.launch {
            val existing = db.watchlistDao().getCoinById(coinId)
            watchlistBtn.text = if (existing != null) "★ Watchlist" else "☆ Watchlist"
        }
        watchlistBtn.setOnClickListener {
            lifecycleScope.launch {
                val existing = db.watchlistDao().getCoinById(coinId)
                if (existing != null) {
                    db.watchlistDao().removeFromWatchlist(existing)
                    watchlistBtn.text = "☆ Watchlist"
                    Toast.makeText(this@DetailActivity, "Removed from watchlist", Toast.LENGTH_SHORT).show()
                } else {
                    currentCoin?.let { coin ->
                        db.watchlistDao().addToWatchlist(coin)
                        watchlistBtn.text = "★ Watchlist"
                        Toast.makeText(this@DetailActivity, "Added to watchlist!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Alert button
        findViewById<Button>(R.id.alertBtn).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                showAlertDialog()
            }
        }

        // Portfolio button
        findViewById<Button>(R.id.portfolioBtn).setOnClickListener {
            showPortfolioDialog(coinId, coinName, coinImage, coinPrice, currency)
        }

        // Share button
        findViewById<Button>(R.id.shareBtn).setOnClickListener {
            val shareText = "$coinName is currently trading at $currencySymbol${String.format("%,.2f", coinPrice)} " +
                    "(${String.format("%.2f", coinChange)}% today) — tracked via MarketLens"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, "Share $coinName price"))
        }

        loadChart(coinId, currency)
    }

    private fun showAlertDialog() {
        val coinId = intent.getStringExtra("COIN_ID") ?: return
        val coinName = intent.getStringExtra("COIN_NAME") ?: ""
        val coinPrice = intent.getDoubleExtra("COIN_PRICE", 0.0)
        val currency = AppCache.selectedCurrency

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Target price"
            setText(String.format("%.2f", coinPrice))
        }

        AlertDialog.Builder(this)
            .setTitle("Set Price Alert for $coinName")
            .setMessage("Alert me when price goes above or below:")
            .setView(input)
            .setPositiveButton("Above") { _, _ ->
                val target = input.text.toString().toDoubleOrNull() ?: return@setPositiveButton
                lifecycleScope.launch {
                    db.alertDao().insertAlert(AlertEntity(coinId = coinId, coinName = coinName, targetPrice = target, isAbove = true, currency = currency))
                    Toast.makeText(this@DetailActivity, "Alert set: $coinName above ${String.format("%.2f", target)}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Below") { _, _ ->
                val target = input.text.toString().toDoubleOrNull() ?: return@setNegativeButton
                lifecycleScope.launch {
                    db.alertDao().insertAlert(AlertEntity(coinId = coinId, coinName = coinName, targetPrice = target, isAbove = false, currency = currency))
                    Toast.makeText(this@DetailActivity, "Alert set: $coinName below ${String.format("%.2f", target)}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun showPortfolioDialog(coinId: String, coinName: String, imageUrl: String, currentPrice: Double, currency: String) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
        }
        val amountInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Amount held (e.g. 0.5)"
        }
        val buyPriceInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Buy price"
            setText(String.format("%.2f", currentPrice))
        }
        layout.addView(amountInput)
        layout.addView(buyPriceInput)

        AlertDialog.Builder(this)
            .setTitle("Add $coinName to Portfolio")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val amount = amountInput.text.toString().toDoubleOrNull() ?: return@setPositiveButton
                val buyPrice = buyPriceInput.text.toString().toDoubleOrNull() ?: return@setPositiveButton
                lifecycleScope.launch {
                    val symbol = AppCache.coinCache[currency]?.first?.find { it.id == coinId }?.symbol ?: ""
                    db.portfolioDao().upsertHolding(
                        PortfolioEntity(coinId = coinId, coinName = coinName, symbol = symbol, imageUrl = imageUrl, amount = amount, buyPrice = buyPrice, currency = currency)
                    )
                    Toast.makeText(this@DetailActivity, "$coinName added to portfolio", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadChart(coinId: String, currency: String) {
        lifecycleScope.launch {
            try {
                val now = System.currentTimeMillis()
                val cached = AppCache.priceHistory[coinId]

                val prices = if (cached != null && (now - cached.second) < AppCache.cacheDuration) {
                    cached.first
                } else {
                    val fetched = RetrofitClient.api.getCoinPriceHistory(coinId, currency, 30)
                    AppCache.priceHistory[coinId] = Pair(fetched, now)
                    fetched
                }

                val entries = prices.prices.mapIndexed { index, price ->
                    Entry(index.toFloat(), price[1].toFloat())
                }

                val dataSet = LineDataSet(entries, "Price (30 days)").apply {
                    color = Color.parseColor("#1DB954")
                    setDrawCircles(false)
                    lineWidth = 2f
                    setDrawFilled(true)
                    fillColor = Color.parseColor("#1DB954")
                    fillAlpha = 30
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawValues(false)
                }

                val chart = findViewById<LineChart>(R.id.lineChart)
                chart.apply {
                    data = LineData(dataSet)
                    description.isEnabled = false
                    legend.isEnabled = false
                    setBackgroundColor(Color.TRANSPARENT)
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.textColor = Color.GRAY
                    xAxis.setDrawGridLines(false)
                    axisLeft.textColor = Color.GRAY
                    axisLeft.setDrawGridLines(false)
                    axisRight.isEnabled = false
                    setTouchEnabled(true)
                    isDragEnabled = true
                    setScaleEnabled(true)
                    animateX(1000)
                    invalidate()
                }

                showForecast(prices.prices)

            } catch (e: Exception) {
                val cached = AppCache.priceHistory[coinId]
                if (cached != null) {
                    showForecast(cached.first.prices)
                } else {
                    val chart = findViewById<LineChart>(R.id.lineChart)
                    val chartError = findViewById<TextView>(R.id.chartError)
                    chart.visibility = View.GONE
                    chartError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showForecast(prices: List<List<Double>>) {
        val currency = AppCache.selectedCurrency
        val currencySymbol = when (currency) { "inr" -> "₹"; "eur" -> "€"; else -> "$" }

        val recentPrices = prices.takeLast(7).map { it[1] }
        val avgChange = recentPrices.zipWithNext { a, b -> b - a }.average()
        val lastPrice = recentPrices.last()
        val forecast7d = lastPrice + (avgChange * 7)

        val forecastView = findViewById<TextView>(R.id.forecastPrice)
        val forecastLabel = findViewById<TextView>(R.id.forecastLabel)

        forecastView.text = "$currencySymbol${String.format("%,.2f", forecast7d)}"
        forecastLabel.text = "7-Day Forecast (Trend Based)"

        val diff = forecast7d - lastPrice
        forecastView.setTextColor(
            if (diff >= 0) Color.parseColor("#1DB954")
            else Color.parseColor("#FF4444")
        )
    }

    private fun formatLargeNumber(value: Double, prefix: String): String {
        return when {
            value >= 1_000_000_000_000 -> "$prefix${String.format("%.2f", value / 1_000_000_000_000)}T"
            value >= 1_000_000_000 -> "$prefix${String.format("%.2f", value / 1_000_000_000)}B"
            value >= 1_000_000 -> "$prefix${String.format("%.2f", value / 1_000_000)}M"
            else -> "$prefix${String.format("%,.0f", value)}"
        }
    }
}
