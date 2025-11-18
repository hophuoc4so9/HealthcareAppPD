package com.example.healthcareapppd.presentation.ui.user.Home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.domain.usecase.Article

class ArticleAdapter(
    private val articles: List<Article>,
    private val onItemClick: (Article) -> Unit
) : RecyclerView.Adapter<ArticleAdapter.HealthArticleViewHolder>() {

    inner class HealthArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivArticleImage: ImageView = itemView.findViewById(R.id.iv_article_image)
        val tvArticleTitle: TextView = itemView.findViewById(R.id.tv_article_title)
        val tvArticleDate: TextView = itemView.findViewById(R.id.tv_article_date)

        fun bind(article: Article) {
            ivArticleImage.setImageResource(article.imageResId)
            tvArticleTitle.text = article.title
            tvArticleDate.text = article.date

            itemView.setOnClickListener {
                onItemClick(article)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return HealthArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: HealthArticleViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int = articles.size
}