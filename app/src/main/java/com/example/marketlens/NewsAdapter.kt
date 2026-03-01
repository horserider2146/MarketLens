package com.example.marketlens

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NewsAdapter(
    private var articles: List<NewsArticle>
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.newsTitle)
        val description: TextView = itemView.findViewById(R.id.newsDescription)
        val source: TextView = itemView.findViewById(R.id.newsSource)
        val date: TextView = itemView.findViewById(R.id.newsDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]
        holder.title.text = article.title
        holder.description.text = article.description ?: ""
        holder.source.text = article.source_name
        holder.date.text = article.pubDate.take(10)

        holder.itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.link))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = articles.size

    fun updateArticles(newArticles: List<NewsArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }
}