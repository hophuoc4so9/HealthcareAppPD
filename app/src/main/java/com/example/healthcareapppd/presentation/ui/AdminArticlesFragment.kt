package com.example.healthcareapppd.presentation.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.presentation.ui.ApiService
import com.example.healthcareapppd.presentation.ui.Article
import com.example.healthcareapppd.utils.TokenManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*

class AdminArticlesFragment : Fragment() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminArticlesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_admin_articles, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewAdminArticles)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = AdminArticlesAdapter(
            onEdit = { article -> showArticleDialog(article) },
            onDelete = { article -> confirmDelete(article) }
        )
        recyclerView.adapter = adapter

        // Nút thêm bài viết
        view.findViewById<FloatingActionButton>(R.id.fabAddArticle).setOnClickListener {
            showArticleDialog(null) // null = Thêm mới
        }

        loadArticles()
        return view
    }

    private fun loadArticles() {
        scope.launch {
            try {
                val token = TokenManager.getToken(requireContext()) ?: return@launch
                val response = withContext(Dispatchers.IO) {
                    ApiService.getArticles(token)
                }
                if (response.success) {
                    adapter.submitList(response.data.articles)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }


    private fun showArticleDialog(article: Article?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_admin_article, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etExternalUrl = dialogView.findViewById<EditText>(R.id.etExternalUrl)
        val etContent = dialogView.findViewById<EditText>(R.id.etContent)
        val spinnerStatus = dialogView.findViewById<Spinner>(R.id.spinnerStatus)

        val statuses = arrayOf("draft", "published")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = spinnerAdapter

        if (article != null) {
            etTitle.setText(article.title)
            // etExternalUrl.setText(article.external_url) // Nếu model có
            // etContent.setText(article.content) // Nếu model có
            val statusIndex = statuses.indexOf(article.status)
            if (statusIndex >= 0) spinnerStatus.setSelection(statusIndex)
        }

        AlertDialog.Builder(context)
            .setTitle(if (article == null) "Thêm bài viết" else "Cập nhật bài viết")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val title = etTitle.text.toString()
                val content = etContent.text.toString()
                val externalUrl = etExternalUrl.text.toString()
                val status = statuses[spinnerStatus.selectedItemPosition]

                if (title.isNotEmpty()) {
                    val data = mapOf(
                        "title" to title,
                        "content" to content,
                        "external_url" to externalUrl,
                        "status" to status
                    )
                    saveArticle(article?.id, data)
                } else {
                    Toast.makeText(context, "Nhập tiêu đề!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun saveArticle(id: String?, data: Map<String, String>) {
        scope.launch {
            val token = TokenManager.getToken(requireContext()) ?: return@launch
            val response = withContext(Dispatchers.IO) {
                if (id == null) ApiService.createArticle(token, data)
                else ApiService.updateArticle(token, id, data)
            }
            if (response.success) {
                Toast.makeText(context, "Thành công", Toast.LENGTH_SHORT).show()
                loadArticles()
            }
        }
    }

    private fun confirmDelete(article: Article) {
        AlertDialog.Builder(context)
            .setTitle("Xóa bài viết?")
            .setMessage("Bạn chắc chắn muốn xóa '${article.title}'?")
            .setPositiveButton("Xóa") { _, _ -> deleteArticle(article.id) }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteArticle(id: String) {
        scope.launch {
            val token = TokenManager.getToken(requireContext()) ?: return@launch
            val response = withContext(Dispatchers.IO) { ApiService.deleteArticle(token, id) }
            if (response.success) {
                Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show()
                loadArticles()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

// Adapter full chức năng cho Articles
class AdminArticlesAdapter(
    private val onEdit: (Article) -> Unit,
    private val onDelete: (Article) -> Unit
) : RecyclerView.Adapter<AdminArticlesAdapter.ViewHolder>() {

    private var list = listOf<Article>()
    fun submitList(l: List<Article>) { list = l; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_article, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvTitle.text = item.title
        holder.tvStatus.text = item.status.uppercase()
        holder.tvDate.text = item.created_at.take(10)

        // Màu status
        if (item.status == "published") {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        } else {
            holder.tvStatus.setTextColor(android.graphics.Color.GRAY)
        }

        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = list.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvArticleTitle)
        val tvStatus: TextView = view.findViewById(R.id.tvArticleStatus)
        val tvDate: TextView = view.findViewById(R.id.tvArticleDate)
        val btnEdit: Button = view.findViewById(R.id.btnEditArticle)
        val btnDelete: Button = view.findViewById(R.id.btnDeleteArticle)
    }
}