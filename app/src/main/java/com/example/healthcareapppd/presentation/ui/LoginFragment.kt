package com.example.healthcareapppd.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.healthcareapppd.LoginActivity
import com.example.healthcareapppd.MainActivity
import com.example.healthcareapppd.data.SessionManager
import com.example.healthcareapppd.data.UserRepository
import com.example.healthcareapppd.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        repository = UserRepository(requireContext())

        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                val email = binding.edtEmail.text.toString().trim()
                val password = binding.edtPassword.text.toString().trim()

                if (repository.loginUser(email, password)) {
                    // ✅ Lưu trạng thái đăng nhập vào SharedPreferences
                    SessionManager.saveUserEmail(requireContext(), email)

                    Toast.makeText(requireContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show()

                    // Chuyển sang MainActivity
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Chuyển sang màn hình đăng ký
        binding.tvSignup.setOnClickListener {
            (activity as? LoginActivity)?.navigateToAuth()
        }

        return binding.root
    }

    private fun validateInputs(): Boolean {
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.edtEmail.error = "Vui lòng nhập email"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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
