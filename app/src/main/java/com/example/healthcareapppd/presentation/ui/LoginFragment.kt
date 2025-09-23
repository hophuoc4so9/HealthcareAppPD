package com.example.healthcareapppd.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.healthcareapppd.AuthActivity
import com.example.healthcareapppd.LoginActivity
import com.example.healthcareapppd.R
import com.example.healthcareapppd.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // Xử lý click Đăng nhập
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                Toast.makeText(requireContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                // TODO: gọi API hoặc chuyển sang màn hình chính
            }
        }

        binding.tvSignup.setOnClickListener {
            val intent = Intent(requireContext(), AuthActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun validateInputs(): Boolean {
        val phone = binding.edtPhone.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()

        if (phone.isEmpty()) {
            binding.edtPhone.error = "Vui lòng nhập số điện thoại"
            return false
        }

        // Regex: số điện thoại VN 10 số, bắt đầu bằng 0
        if (!phone.matches(Regex("^0\\d{9}$"))) {
            binding.edtPhone.error = "Số điện thoại không hợp lệ"
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
