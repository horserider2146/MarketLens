package com.example.marketlens

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var coinAdapter: SearchCoinAdapter
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var sectionLabel: TextView
    private lateinit var progressBar: ProgressBar
    private var searchJob: Job? = null
    private val NEWS_API_KEY = "pub_c88dffe7b3ac46d090b6eb3a044658e5"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchInput = view.findViewById<EditText>(R.id.searchInput)
        recyclerView = view.findViewById(R.id.recyclerViewSearch)
        sectionLabel = view.findViewById(R.id.sectionLabel)
        progressBar = view.findViewById(R.id.newsProgressBar)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Setup coin adapter
        coinAdapter = SearchCoinAdapter(emptyList()) { coin ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("COIN_ID", coin.id)
                putExtra("COIN_NAME", coin.name)
                putExtra("COIN_PRICE", 0.0)
                putExtra("COIN_CHANGE", 0.0)
            }
            startActivity(intent)
        }

        // Setup news adapter
        newsAdapter = NewsAdapter(emptyList())

        // Show news by default
        showNews()

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    // Show news when search is cleared
                    showNews()
                } else {
                    searchJob = lifecycleScope.launch {
                        delay(500)
                        searchCoins(query)
                    }
                }
            }
        })

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchInput.text.toString().trim()
                if (query.isNotEmpty()) searchCoins(query)
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                        as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(searchInput.windowToken, 0)
                true
            } else false
        }
    }

    private fun showNews() {
        sectionLabel.text = "Latest Crypto News"
        recyclerView.adapter = newsAdapter
        if (newsAdapter.itemCount == 0) {
            loadNews()
        }
    }

    private fun loadNews() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = NewsRetrofitClient.api.getCryptoNews(NEWS_API_KEY)
                newsAdapter.updateArticles(response.results)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Could not load news", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun searchCoins(query: String) {
        sectionLabel.text = "Search Results"
        recyclerView.adapter = coinAdapter
        lifecycleScope.launch {
            try {
                val result = RetrofitClient.api.searchCoins(query)
                coinAdapter.updateCoins(result.coins)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}