package com.example.healthcareapppd.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.healthcareapppd.AuthActivity
import com.example.healthcareapppd.LoginActivity
import com.example.healthcareapppd.MainActivity
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.api.RetrofitClient
import com.example.healthcareapppd.data.api.model.LoginRequest
import com.example.healthcareapppd.databinding.FragmentLoginBinding
import com.example.healthcareapppd.utils.TokenManager
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())

        // Xử lý click Đăng nhập
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }

        binding.tvSignup.setOnClickListener {
            val intent = Intent(requireContext(), AuthActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun performLogin() {
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()

        // Hiển thị loading
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Đang đăng nhập..."

        lifecycleScope.launch {
            try {
                val loginRequest = LoginRequest(email, password)
                val response = RetrofitClient.authApi.login(loginRequest)

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
                        "Đăng nhập thành công",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Chuyển đến MainActivity
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    (activity as? LoginActivity)?.finish()
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.message ?: "Đăng nhập thất bại",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Đăng nhập"
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Đăng nhập"
            }
        }
    }

    private fun validateInputs(): Boolean {
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.edtEmail.error = "Vui lòng nhập email"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.error = "Email không hợp lệ"
            return false
        }

        if (TextUtils.isEmpty(password)) {
            binding.edtPassword.error = "Vui lòng nhập mật khẩu"
            return false
        }

        if (password.length < 6) {
            binding.edtPassword.error = "Mật khẩu tối thiểu 6 ký tự"
            return false
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
