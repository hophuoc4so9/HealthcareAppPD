package com.example.healthcareapppd.presentation.ui.user.Home

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.databinding.ItemArticleBinding
import com.example.healthcareapppd.data.api.model.Article

class ArticleAdapter(
    private val articles: List<Article>,
    private val onArticleClick: (Article) -> Unit
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onArticleClick(articles[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]
        holder.binding.tvArticleTitle.text = article.title
        holder.binding.tvArticleDescription.text = article.contentBody ?: article.content ?: article.externalUrl
    }

    override fun getItemCount(): Int {
        return articles.size
    }
}