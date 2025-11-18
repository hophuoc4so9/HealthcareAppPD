package com.example.healthcareapppd.presentation.ui.Auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.healthcareapppd.AuthActivity
import com.example.healthcareapppd.LoginActivity
import com.example.healthcareapppd.data.UserRepository
import com.example.healthcareapppd.data.SessionManager
import com.example.healthcareapppd.databinding.FragmentAuthBinding
import com.example.healthcareapppd.domain.usecase.User

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        repository = UserRepository(requireContext())  // khởi tạo

        // Xử lý nút đăng ký
        binding.btnSignup.setOnClickListener {
            if (validateForm()) {
                val username = binding.edtUsername.text.toString().trim()
                val phone = binding.edtPhone.text.toString().trim()
                val email = binding.edtEmail.text.toString().trim()
                val password = binding.edtPassword.text.toString()

                val newUser = User(
                    username = username,
                    phone = phone,
                    email = email,
                    password = password
                )

                val success = repository.registerUser(newUser)
                if (success) {
                    // Lưu session
                    SessionManager.saveUserEmail(requireContext(), email)

                    showToast("Đăng ký thành công!")
                    (activity as? AuthActivity)?.navigateToMain()
                } else {
                    showToast("Email đã tồn tại, vui lòng dùng email khác")
                }
            }
        }

        // Link sang màn hình đăng nhập
        binding.tvLogin.setOnClickListener {
            (activity as? AuthActivity)?.navigateToLogin()
        }

        return binding.root
    }

    // Validate form
    private fun validateForm(): Boolean {
        val username = binding.edtUsername.text.toString().trim()
        val phone = binding.edtPhone.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString()
        val confirmPassword = binding.edtConfirmPassword.text.toString()

        if (username.isEmpty() || phone.isEmpty() || email.isEmpty()
            || password.isEmpty() || confirmPassword.isEmpty()
        ) {
            showToast("Vui lòng nhập đầy đủ thông tin")
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email không hợp lệ")
            return false
        }

        if (!phone.matches(Regex("^[0-9]+$"))) {
            showToast("Số điện thoại chỉ được chứa số")
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
}
