package com.example.marketlens

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var currentCoin: WatchlistEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        db = AppDatabase.getDatabase(this)

        val coinId = intent.getStringExtra("COIN_ID") ?: return
        val coinName = intent.getStringExtra("COIN_NAME") ?: ""
        val coinPrice = intent.getDoubleExtra("COIN_PRICE", 0.0)
        val coinChange = intent.getDoubleExtra("COIN_CHANGE", 0.0)
        val coinImage = intent.getStringExtra("COIN_IMAGE") ?: ""

        currentCoin = WatchlistEntity(
            id = coinId,
            name = coinName,
            symbol = "",
            imageUrl = coinImage,
            currentPrice = coinPrice,
            priceChange24h = coinChange
        )

        findViewById<TextView>(R.id.detailName).text = coinName
        findViewById<TextView>(R.id.detailPrice).text = "$${String.format("%,.2f", coinPrice)}"

        val changeView = findViewById<TextView>(R.id.detailChange)
        changeView.text = "${String.format("%.2f", coinChange)}%"
        changeView.setTextColor(
            if (coinChange >= 0) Color.parseColor("#1DB954")
            else Color.parseColor("#FF4444")
        )

        val watchlistBtn = findViewById<Button>(R.id.watchlistBtn)

        lifecycleScope.launch {
            val existing = db.watchlistDao().getCoinById(coinId)
            watchlistBtn.text = if (existing != null) "★ Remove from Watchlist" else "☆ Add to Watchlist"
        }

        watchlistBtn.setOnClickListener {
            lifecycleScope.launch {
                val existing = db.watchlistDao().getCoinById(coinId)
                if (existing != null) {
                    db.watchlistDao().removeFromWatchlist(existing)
                    watchlistBtn.text = "☆ Add to Watchlist"
                    Toast.makeText(this@DetailActivity, "Removed from watchlist", Toast.LENGTH_SHORT).show()
                } else {
                    currentCoin?.let { coin ->
                        db.watchlistDao().addToWatchlist(coin)
                        watchlistBtn.text = "★ Remove from Watchlist"
                        Toast.makeText(this@DetailActivity, "Added to watchlist!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        loadChart(coinId)
    }

    private fun loadChart(coinId: String) {
        lifecycleScope.launch {
            try {
                val now = System.currentTimeMillis()
                val cached = AppCache.priceHistory[coinId]

                val prices = if (cached != null && (now - cached.second) < AppCache.cacheDuration) {
                    cached.first
                } else {
                    val fetched = RetrofitClient.api.getCoinPriceHistory(coinId, "usd", 30)
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
                    setBackgroundColor(Color.parseColor("#121212"))
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.textColor = Color.WHITE
                    xAxis.setDrawGridLines(false)
                    axisLeft.textColor = Color.WHITE
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
                    Toast.makeText(this@DetailActivity, "Showing cached data", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DetailActivity, "Rate limited. Please wait a moment.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showForecast(prices: List<List<Double>>) {
        val recentPrices = prices.takeLast(7).map { it[1] }
        val avgChange = recentPrices.zipWithNext { a, b -> b - a }.average()
        val lastPrice = recentPrices.last()
        val forecast7d = lastPrice + (avgChange * 7)

        val forecastView = findViewById<TextView>(R.id.forecastPrice)
        val forecastLabel = findViewById<TextView>(R.id.forecastLabel)

        forecastView.text = "$${String.format("%,.2f", forecast7d)}"
        forecastLabel.text = "7-Day Forecast (Trend Based)"

        val diff = forecast7d - lastPrice
        forecastView.setTextColor(
            if (diff >= 0) Color.parseColor("#1DB954")
            else Color.parseColor("#FF4444")
        )
    }
}