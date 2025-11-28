package com.example.healthcareapppd.presentation.ui.user.Home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log // Thêm import Log
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

    // Tag để lọc log trong Logcat
    private val TAG = "HomeFragmentDebug"

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
        Log.d(TAG, "onViewCreated: Fragment started") // Log 1

        tokenManager = TokenManager(requireContext())

        // Setup Listeners
        binding.layoutDoctor.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_docter)
        }
        binding.layoutMap.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_map)
        }
        binding.searchBox.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_docter)
        }
        binding.searchBox.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                findNavController().navigate(R.id.action_home_to_docter)
            }
        }

        // Gọi load dữ liệu
        loadUserProfile()
        loadArticles()
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            val token = tokenManager.getToken()
            Log.d(TAG, "loadUserProfile: Token present = ${token != null}") // Log 2

            if (token != null) {
                val result = getPatientProfileUseCase(token)
                result.onSuccess { profile ->
                    Log.d(TAG, "loadUserProfile: Success - ${profile.fullName}")
                    // Cập nhật UI an toàn với binding
                    binding.tvUserNameHome.text = profile.fullName
                }.onFailure { error ->
                    Log.e(TAG, "loadUserProfile: Failed", error)
                }
            }
        }
    }

    private fun loadArticles() {
        Log.d(TAG, "loadArticles: Starting API call...") // Log 3

        lifecycleScope.launch {
            // Gọi API
            val result = getAllArticlesUseCase(page = 1, limit = 10, status = "published")

            result.onSuccess { response ->
                Log.d(TAG, "loadArticles: API Success. Response: $response") // Log 4

                val articlesList = response.data?.articles
                Log.d(TAG, "loadArticles: Articles list size = ${articlesList?.size ?: 0}") // Log 5

                if (articlesList != null && articlesList.isNotEmpty()) {
                    setupArticleRecyclerView(articlesList)
                } else {
                    Log.w(TAG, "loadArticles: List empty or null")
                    Toast.makeText(requireContext(), "Không có bài viết nào", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { error ->
                Log.e(TAG, "loadArticles: API Failed", error) // Log 6
                Toast.makeText(requireContext(), "Lỗi tải bài viết: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupArticleRecyclerView(articles: List<Article>) {
        Log.d(TAG, "setupArticleRecyclerView: Setting up adapter with ${articles.size} items") // Log 7

        val articleAdapter = ArticleAdapter(articles) { article ->
            handleArticleClick(article)
        }

        binding.recyclerArticles.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = articleAdapter
            setHasFixedSize(true) // Tối ưu hiệu năng
        }

        // Kiểm tra xem RecyclerView có hiển thị không
        Log.d(TAG, "setupArticleRecyclerView: Adapter attached. Visibility = ${binding.recyclerArticles.visibility}")
    }

    private fun handleArticleClick(article: Article) {
        Log.d(TAG, "handleArticleClick: Clicked article ${article.title}")

        if (!article.externalUrl.isNullOrBlank()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.externalUrl))
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "handleArticleClick: Error opening URL", e)
                Toast.makeText(requireContext(), "Không thể mở link", Toast.LENGTH_SHORT).show()
            }
        } else if (!article.contentBody.isNullOrBlank()) {
            showArticleContent(article)
        } else {
            Toast.makeText(requireContext(), "Bài viết không có nội dung", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showArticleContent(article: Article) {
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