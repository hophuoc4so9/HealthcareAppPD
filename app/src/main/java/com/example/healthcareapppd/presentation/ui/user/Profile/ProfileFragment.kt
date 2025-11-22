package com.example.healthcareapppd.presentation.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.healthcareapppd.R
import com.example.healthcareapppd.WelcomeActivity
import com.example.healthcareapppd.utils.TokenManager
import com.example.healthcareapppd.domain.usecase.patient.GetPatientProfileUseCase
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var menuSchedule: LinearLayout
    private lateinit var menuFaq: LinearLayout
    private lateinit var menuLogout: LinearLayout
    private val getPatientProfileUseCase = GetPatientProfileUseCase()
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        tokenManager = TokenManager(requireContext())
        initViews(view)
        setupClickListeners()
        loadUserData()
        return view
    }

    private fun initViews(view: View) {
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        menuSchedule = view.findViewById(R.id.menuSchedule)
        menuFaq = view.findViewById(R.id.menuFaq)
        menuLogout = view.findViewById(R.id.menuLogout)
    }

    private fun setupClickListeners() {
        menuSchedule.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_viewPatientProfileFragment)
        }

        menuFaq.setOnClickListener {
            Toast.makeText(requireContext(), "Mở màn hình FAQ", Toast.LENGTH_SHORT).show()
        }

        // Click vào Logout
        menuLogout.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        // Xóa token
        val tokenManager = TokenManager(requireContext())
        tokenManager.clearToken()
        
        // Hiển thị thông báo
        Toast.makeText(requireContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
        
        // Chuyển về WelcomeActivity và xóa toàn bộ back stack
        val intent = Intent(requireContext(), WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val token = tokenManager.getToken()
            if (token != null) {
                val result = getPatientProfileUseCase(token)
                result.onSuccess { profile ->
                    tvUserName.text = profile.fullName
                    tvUserEmail.text = profile.email ?: "Email không có"
                }.onFailure { error ->
                    // Hiển thị giá trị mặc định nếu lỗi
                    tvUserName.text = "Người dùng"
                    tvUserEmail.text = "email@example.com"
                    Toast.makeText(
                        requireContext(),
                        "Không thể tải thông tin: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                tvUserName.text = "Người dùng"
                tvUserEmail.text = "email@example.com"
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}