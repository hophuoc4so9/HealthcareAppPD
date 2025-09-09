package com.example.healthcareapppd.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.healthcareapppd.AuthActivity
import com.example.healthcareapppd.R

class AuthFragment : Fragment() {

    private lateinit var edtUsername: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var checkTerms: CheckBox
    private lateinit var tvTerms: TextView
    private lateinit var tvAcc: TextView
    private lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auth, container, false)

        edtUsername = view.findViewById(R.id.edtUsername)
        edtEmail = view.findViewById(R.id.edtEmail)
        edtPassword = view.findViewById(R.id.edtPassword)
        checkTerms = view.findViewById(R.id.checkTerms)
        tvTerms = view.findViewById(R.id.tvTerms)
        tvAcc = view.findViewById(R.id.tvAcc)
        textView = view.findViewById(R.id.textView)
        btnLogin = view.findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!checkTerms.isChecked) {
                Toast.makeText(requireContext(), "Bạn cần đồng ý với điều khoản dịch vụ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(requireContext(), "Đăng ký thành công!\nTên: $username\nEmail: $email", Toast.LENGTH_LONG).show()

            (activity as? AuthActivity)?.navigateToMain()

        }

        return view
    }
}
