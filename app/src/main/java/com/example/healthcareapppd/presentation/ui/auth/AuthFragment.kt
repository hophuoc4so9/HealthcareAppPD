package com.example.healthcareapppd.presentation.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.healthcareapppd.AuthActivity
import com.example.healthcareapppd.MainActivity
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.RegisterRequest
import com.example.healthcareapppd.databinding.FragmentAuthBinding
import com.example.healthcareapppd.utils.TokenManager
import kotlinx.coroutines.launch

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())

        // Sự kiện nút đăng ký
        binding.btnSignup.setOnClickListener {
            if (validateForm()) {
                performRegister()
            }
        }

        binding.tvLogin.setOnClickListener {
            val intent = Intent(requireContext(), AuthActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun performRegister() {
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()

        // Hiển thị loading
        binding.btnSignup.isEnabled = false
        binding.btnSignup.text = "Đang đăng ký..."

        lifecycleScope.launch {
            try {
                val registerRequest = RegisterRequest(
                    email = email,
                    password = password,
                    role = "patient" // Mặc định role là patient
                )
                val response = RetrofitClient.authApi.register(registerRequest)

                if (response.success && response.data != null) {
                    // Lưu token và thông tin user
                    tokenManager.saveToken(response.data.token)
                    tokenManager.saveUserInfo(
                        response.data.user.id,
                        response.data.user.email,
                        response.data.user.role
                    )

                    Toast.makeText(
                        requireContext(),
                        "Đăng ký thành công",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Chuyển đến MainActivity
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    (activity as? AuthActivity)?.finish()
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.message ?: "Đăng ký thất bại",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnSignup.isEnabled = true
                    binding.btnSignup.text = "Đăng ký"
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.btnSignup.isEnabled = true
                binding.btnSignup.text = "Đăng ký"
            }
        }
    }

    // Hàm validate form
    private fun validateForm(): Boolean {
        val username = binding.edtUsername.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString()
        val confirmPassword = binding.edtConfirmPassword.text.toString()

        if (username.isEmpty() || email.isEmpty()
            || password.isEmpty() || confirmPassword.isEmpty()
        ) {
            showToast("Vui lòng nhập đầy đủ thông tin")
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email không hợp lệ")
            return false
        }

        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")
        if (!passwordRegex.matches(password)) {
            showToast("Mật khẩu phải ≥ 8 ký tự, có chữ hoa, chữ thường và số")
            return false
        }

        if (password != confirmPassword) {
            showToast("Mật khẩu xác nhận không khớp")
            return false
        }

        if (!binding.checkTerms.isChecked) {
            showToast("Bạn cần đồng ý điều khoản của chúng tôi")
            return false
        }

        return true
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
