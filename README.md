# 📈 MarketLens
### Crypto & Stock Trend Visualizer with ML Forecasting

![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue?logo=kotlin)
![API](https://img.shields.io/badge/API-24%2B-brightgreen)
![License](https://img.shields.io/badge/License-MIT-yellow)

MarketLens is a real-time cryptocurrency tracking Android application built with Kotlin. It allows users to monitor live crypto prices, view interactive 30-day price charts, get 7-day trend-based forecasts, search any coin, and save favourites to a personal watchlist.

---

## 📱 Screenshots

> Market Screen | Detail Screen | Watchlist Screen

*(Add your screenshots here)*

---

## ✨ Features

- **Live Market Data** — Real-time prices for top 50 cryptocurrencies powered by CoinGecko API
- **Interactive Price Charts** — 30-day historical price chart with zoom and drag support
- **7-Day Forecast** — Trend-based price prediction using moving average analysis
- **Search** — Search any cryptocurrency by name with debounced live search
- **Watchlist** — Save and manage favourite coins using local Room database
- **Caching** — Smart API response caching to avoid rate limits
- **Material Design** — Dark themed UI following Material Design 3 guidelines
- **Splash Screen** — Branded app launch screen

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin |
| UI | XML Layouts, Material Design 3 |
| Networking | Retrofit 2 + OkHttp |
| API | CoinGecko (Free, no key required) |
| Charts | MPAndroidChart |
| Database | Room (local watchlist storage) |
| Image Loading | Glide |
| ML/Forecasting | Trend-based moving average algorithm |
| Async | Kotlin Coroutines |
| Architecture | MVVM-inspired with LiveData |

---

## 🏗️ Project Structure

```
app/src/main/java/com/example/marketlens/
│
├── data/
│   ├── Coin.kt                  # Crypto data model
│   ├── PriceHistory.kt          # Price history model
│   ├── SearchResponse.kt        # Search response model
│   ├── WatchlistEntity.kt       # Room entity
│   ├── WatchlistDao.kt          # Room DAO
│   └── AppDatabase.kt           # Room database
│
├── network/
│   ├── CoinGeckoApi.kt          # Retrofit API interface
│   └── RetrofitClient.kt        # Retrofit singleton
│
├── ui/
│   ├── MainActivity.kt          # Bottom navigation host
│   ├── HomeFragment.kt          # Market overview
│   ├── SearchFragment.kt        # Coin search
│   ├── WatchlistFragment.kt     # Saved coins
│   ├── DetailActivity.kt        # Chart + forecast
│   └── SplashActivity.kt        # Launch screen
│
├── adapter/
│   ├── CoinAdapter.kt           # Market list adapter
│   ├── SearchCoinAdapter.kt     # Search results adapter
│   └── WatchlistAdapter.kt      # Watchlist adapter
│
└── util/
    └── AppCache.kt              # In-memory API cache
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- Android device or emulator running API 24+
- Internet connection

### Installation

1. Clone the repository:
```bash
git clone https://github.com/YOUR_USERNAME/MarketLens.git
```

2. Open the project in Android Studio

3. Let Gradle sync complete

4. Run the app on your device or emulator

### Download APK
Download the latest APK directly from the [Releases](../../releases) page and install it on your Android device.

---

## 📡 API Reference

This app uses the **CoinGecko API** (free tier, no API key required):

| Endpoint | Usage |
|---|---|
| `/coins/markets` | Fetch top 50 coins by market cap |
| `/coins/{id}/market_chart` | Fetch 30-day price history |
| `/search` | Search coins by name |

> **Rate Limiting:** CoinGecko free tier allows ~10-30 calls/minute. The app implements 2-minute caching to stay within limits.

---

## 🔮 Forecasting Algorithm

The 7-day price forecast uses a **trend-based moving average** approach:

1. Takes the last 7 days of price data
2. Calculates the average daily price change
3. Projects that average change forward 7 days
4. Displays result in green (uptrend) or red (downtrend)

This is a simple but effective baseline forecasting method suitable for trend visualization.

---

## 📋 Project Requirements Mapping

This project was built as part of a Data Science course project (40 marks):

| Rubric Criteria | Implementation |
|---|---|
| Originality & Self-Learning | CoinGecko API integration + custom forecasting algorithm |
| Societal Problem | Helps everyday users track and forecast crypto investments |
| Standardized Workable App | Published APK, Material Design, Play Store ready |
| Material Design | Dark theme, MaterialCardView, BottomNavigationView |
| Team Contribution | Solo development — full documentation of design decisions |
| Viva/Demo Ready | Live data, interactive charts, explainable forecast logic |

---

## 📦 Dependencies

```kotlin
// Retrofit
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")

// MPAndroidChart
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

// Glide
implementation("com.github.bumptech.glide:glide:4.16.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

---

## 👤 Author

**Ritarshi Roy**
- Data Science Student
- GitHub: [@YOUR_USERNAME](https://github.com/YOUR_USERNAME)

---

## 📄 License

This project is licensed under the MIT License.

---

> Built using Kotlin and CoinGecko API
