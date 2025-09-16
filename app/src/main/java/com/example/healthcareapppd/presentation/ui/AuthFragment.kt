package com.example.healthcareapppd.presentation.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.healthcareapppd.AuthActivity
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.core.content.ContextCompat
import com.example.healthcareapppd.LoginActivity
import com.example.healthcareapppd.R

class AuthFragment : Fragment() {

    private lateinit var edtUsername: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtConfirmPassword: EditText
    private lateinit var btnSignup: Button
    private lateinit var checkTerms: CheckBox
    private lateinit var tvTerms: TextView
    private lateinit var tvAcc: TextView
    private lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auth, container, false)

        // Ánh xạ View
        edtUsername = view.findViewById(R.id.edtUsername)
        edtPhone = view.findViewById(R.id.edtPhone)
        edtEmail = view.findViewById(R.id.edtEmail)
        edtPassword = view.findViewById(R.id.edtPassword)
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword)
        checkTerms = view.findViewById(R.id.checkTerms)
        tvTerms = view.findViewById(R.id.tvTerms)
        tvAcc = view.findViewById(R.id.tvAcc)
        textView = view.findViewById(R.id.textView)
        btnSignup = view.findViewById(R.id.btnSignup)

        // Bật/tắt hiện mật khẩu
        setupPasswordToggle(edtPassword)
        setupPasswordToggle(edtConfirmPassword)

        // Sự kiện nút đăng ký
        btnSignup.setOnClickListener {
            if (validateForm()) {
                // Nếu hợp lệ → sang MainActivity
                (activity as? AuthActivity)?.navigateToMain()
            }
        }

        textView.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish() // đóng trang đăng ký, tránh quay lại
        }

        return view
    }

    // Hàm validate form
    private fun validateForm(): Boolean {
        val username = edtUsername.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString()
        val confirmPassword = edtConfirmPassword.text.toString()

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

        if (!checkTerms.isChecked) {
            showToast("Bạn cần đồng ý điều khoản của chúng tôi")
            return false
        }

        return true
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    // Hàm bật/tắt hiện mật khẩu
    @SuppressLint("ClickableViewAccessibility")
    private fun setupPasswordToggle(editText: EditText) {
        var isPasswordVisible = false

        editText.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val drawableEnd = editText.compoundDrawables[2] // icon bên phải
                if (drawableEnd != null &&
                    event.rawX >= (editText.right - drawableEnd.bounds.width() - editText.paddingEnd)
                ) {
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        // Hiện mật khẩu
                        editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                        editText.setCompoundDrawablesWithIntrinsicBounds(
                            editText.compoundDrawables[0], null,
                            ContextCompat.getDrawable(requireContext(), R.drawable.baseline_visibility_24),
                            null
                        )
                    } else {
                        // Ẩn mật khẩu
                        editText.transformationMethod = PasswordTransformationMethod.getInstance()
                        editText.setCompoundDrawablesWithIntrinsicBounds(
                            editText.compoundDrawables[0], null,
                            ContextCompat.getDrawable(requireContext(), R.drawable.baseline_visibility_off_24),
                            null
                        )
                    }
                    editText.setSelection(editText.text.length) // Giữ con trỏ ở cuối
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
}
