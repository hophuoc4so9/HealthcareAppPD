package com.example.healthcareapppd.presentation.ui.user.Home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapppd.R
import com.example.healthcareapppd.databinding.FragmentHomeBinding
import com.example.healthcareapppd.domain.usecase.article.GetAllArticlesUseCase
import com.example.healthcareapppd.domain.usecase.patient.GetPatientProfileUseCase
import com.example.healthcareapppd.data.api.model.Article
import com.example.healthcareapppd.utils.TokenManager
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val getAllArticlesUseCase = GetAllArticlesUseCase()
    private val getPatientProfileUseCase = GetPatientProfileUseCase()
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())

        binding.layoutDoctor.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_docter)
        }
        binding.layoutMap.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_map)
        }

        // Khi click vào thanh tìm kiếm, chuyển sang màn hình danh sách bác sĩ
        binding.searchBox.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_docter)
        }
        binding.searchBox.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                findNavController().navigate(R.id.action_home_to_docter)
            }
        }

        loadUserProfile()
        loadArticles()
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            val token = tokenManager.getToken()
            if (token != null) {
                val result = getPatientProfileUseCase(token)
                result.onSuccess { profile ->
                    // Cập nhật tên trong layout
                    view?.findViewById<TextView>(R.id.tvUserNameHome)?.text = profile.fullName
                }.onFailure {
                    // Xử lý lỗi nếu cần
                }
            }
        }
    }

    private fun loadArticles() {
        lifecycleScope.launch {
            val result = getAllArticlesUseCase(page = 1, limit = 10, status = "published")
            
            result.onSuccess { response ->
                if (response.data?.articles != null && response.data.articles.isNotEmpty()) {
                    setupArticleRecyclerView(response.data.articles)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Không có bài viết nào",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Lỗi: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupArticleRecyclerView(articles: List<*>) {
        val articleAdapter = ArticleAdapter(
            articles as List<Article>
        ) { article ->
            handleArticleClick(article)
        }

        binding.recyclerArticles.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = articleAdapter
        }
    }

    private fun handleArticleClick(article: Article) {
        if (!article.externalUrl.isNullOrBlank()) {
            // Mở link external_url trong trình duyệt
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.externalUrl))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Không thể mở link",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (!article.contentBody.isNullOrBlank()) {
            // Hiển thị content_body (có thể tạo dialog hoặc navigate đến màn hình detail)
            showArticleContent(article)
        } else {
            Toast.makeText(
                requireContext(),
                "Bài viết không có nội dung",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showArticleContent(article: Article) {
        // Tạo dialog hoặc bottom sheet để hiển thị nội dung
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(article.title)
            .setMessage(article.contentBody)
            .setPositiveButton("Đóng", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}