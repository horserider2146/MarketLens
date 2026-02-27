package com.example.marketlens

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var adapter: SearchCoinAdapter
    private var searchJob: Job? = null

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
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSearch)

        adapter = SearchCoinAdapter(emptyList()) { coin ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("COIN_ID", coin.id)
                putExtra("COIN_NAME", coin.name)
                putExtra("COIN_PRICE", 0.0)
                putExtra("COIN_CHANGE", 0.0)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Search with debounce — waits 500ms after user stops typing
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(500)
                    val query = s.toString().trim()
                    if (query.isNotEmpty()) {
                        searchCoins(query)
                    }
                }
            }
        })
    }

    private fun searchCoins(query: String) {
        lifecycleScope.launch {
            try {
                val result = RetrofitClient.api.searchCoins(query)
                adapter.updateCoins(result.coins)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}