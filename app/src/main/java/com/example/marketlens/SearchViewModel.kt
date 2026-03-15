package com.example.marketlens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    sealed class UiMode {
        data class NewsMode(
            val articles: List<NewsArticle>,
            val isLoading: Boolean,
            val hasError: Boolean = false
        ) : UiMode()
        data class SearchMode(
            val coins: List<SearchCoin>,
            val isLoading: Boolean,
            val query: String
        ) : UiMode()
    }

    private val _mode = MutableStateFlow<UiMode>(UiMode.NewsMode(emptyList(), isLoading = true))
    val mode: StateFlow<UiMode> = _mode

    private var searchJob: Job? = null
    private var cachedNews: List<NewsArticle> = emptyList()
    private var newsLoaded = false

    init {
        loadNews()
    }

    private fun loadNews() {
        if (newsLoaded && cachedNews.isNotEmpty()) {
            _mode.value = UiMode.NewsMode(cachedNews, isLoading = false)
            return
        }
        _mode.value = UiMode.NewsMode(emptyList(), isLoading = true)
        viewModelScope.launch {
            try {
                val response = NewsRetrofitClient.api.getCryptoNews(NEWS_API_KEY)
                cachedNews = response.results
                newsLoaded = true
                _mode.value = UiMode.NewsMode(cachedNews, isLoading = false)
            } catch (e: Exception) {
                _mode.value = UiMode.NewsMode(emptyList(), isLoading = false, hasError = true)
            }
        }
    }

    fun onQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.isEmpty()) {
            if (_mode.value is UiMode.SearchMode) {
                if (newsLoaded && cachedNews.isNotEmpty()) {
                    _mode.value = UiMode.NewsMode(cachedNews, isLoading = false)
                } else {
                    loadNews()
                }
            }
            return
        }
        searchJob = viewModelScope.launch {
            delay(500)
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        _mode.value = UiMode.SearchMode(emptyList(), isLoading = true, query = query)
        viewModelScope.launch {
            try {
                val result = RetrofitClient.api.searchCoins(query)
                _mode.value = UiMode.SearchMode(result.coins, isLoading = false, query = query)
            } catch (e: Exception) {
                _mode.value = UiMode.SearchMode(emptyList(), isLoading = false, query = query)
            }
        }
    }

    companion object {
        private val NEWS_API_KEY get() = BuildConfig.NEWS_API_KEY
    }
}
