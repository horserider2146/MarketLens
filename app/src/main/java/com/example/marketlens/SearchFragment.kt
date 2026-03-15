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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var coinAdapter: SearchCoinAdapter
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var sectionLabel: TextView
    private lateinit var progressBar: ProgressBar
    private val viewModel: SearchViewModel by viewModels()

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

        coinAdapter = SearchCoinAdapter(emptyList()) { coin ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("COIN_ID", coin.id)
                putExtra("COIN_NAME", coin.name)
                putExtra("COIN_PRICE", 0.0)
                putExtra("COIN_CHANGE", 0.0)
            }
            startActivity(intent)
        }

        newsAdapter = NewsAdapter(emptyList())

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onQueryChanged(s.toString().trim())
            }
        })

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchInput.text.toString().trim()
                if (query.isNotEmpty()) viewModel.onQueryChanged(query)
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                        as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(searchInput.windowToken, 0)
                true
            } else false
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mode.collect { mode ->
                    when (mode) {
                        is SearchViewModel.UiMode.NewsMode -> {
                            sectionLabel.text = "Latest Crypto News"
                            recyclerView.adapter = newsAdapter
                            progressBar.visibility = if (mode.isLoading) View.VISIBLE else View.GONE
                            if (!mode.isLoading) newsAdapter.updateArticles(mode.articles)
                        }
                        is SearchViewModel.UiMode.SearchMode -> {
                            sectionLabel.text = "Search Results"
                            recyclerView.adapter = coinAdapter
                            progressBar.visibility = if (mode.isLoading) View.VISIBLE else View.GONE
                            if (!mode.isLoading) coinAdapter.updateCoins(mode.coins)
                        }
                    }
                }
            }
        }
    }
}
