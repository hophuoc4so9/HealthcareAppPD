package com.example.healthcareapppd.presentation.ui.user.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.SessionManager
import com.example.healthcareapppd.databinding.FragmentHomeBinding
import com.example.healthcareapppd.domain.usecase.Article
import com.example.healthcareapppd.presentation.ui.user.Home.ArticleAdapter

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userEmail = SessionManager.getUserEmail(requireContext())

        if (userEmail != null) {
            // Hiển thị lời chào mừng người dùng
            Toast.makeText(requireContext(), "Xin chào $userEmail!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show()
        }

        // Xử lý sự kiện khi nhấn vào Layout Doctor
        binding.layoutDoctor.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_docter)
        }

        // Xử lý sự kiện khi nhấn vào Layout Hospital
//        binding.layoutHospital.setOnClickListener {
//            findNavController().navigate(R.id.action_home_to_hospital)
//        }

        // Setup RecyclerView hiển thị danh sách bài viết sức khỏe
        val articles = listOf(
            Article("10 cách tăng cường sức đề kháng", "04/10/2025", R.drawable.ic_article1),
            Article("Chế độ ăn lành mạnh cho sinh viên", "03/10/2025", R.drawable.ic_article1),
            Article("Lợi ích của việc tập thể dục buổi sáng", "02/10/2025", R.drawable.ic_article2),
            Article("Ảnh hưởng của Covid-19 đến sức khỏe toàn cầu", "02/10/2025", R.drawable.ic_article2),
            Article("Lợi ích của việc tập thể dục buổi sáng", "02/10/2025", R.drawable.ic_article2)
        )

        binding.rvHealthArticles.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvHealthArticles.adapter = ArticleAdapter(articles) { article ->
            // TODO: xử lý khi click vào 1 bài viết (ví dụ: mở màn hình chi tiết)
        }

        // Xử lý sự kiện khi nhấn "Xem tất cả"
        binding.tvSeeAllArticles.setOnClickListener {
            // TODO: Điều hướng đến màn hình danh sách bài viết đầy đủ
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}