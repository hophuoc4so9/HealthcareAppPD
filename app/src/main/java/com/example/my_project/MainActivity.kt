package com.example.my_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.example.my_project.R

class MainActivity : AppCompatActivity() {

    private lateinit var edtUsername: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var checkTerms: CheckBox
    private lateinit var tvTerms: TextView
    private lateinit var tvAcc: TextView
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ánh xạ
        edtUsername = findViewById(R.id.edtUsername)
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        checkTerms = findViewById(R.id.checkTerms)
        tvTerms = findViewById(R.id.tvTerms)
        tvAcc = findViewById(R.id.tvAcc)
        textView = findViewById(R.id.textView)
        btnLogin = findViewById(R.id.btnLogin)

        // Sự kiện nút đăng ký
        btnLogin.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!checkTerms.isChecked) {
                Toast.makeText(this, "Bạn cần đồng ý với điều khoản dịch vụ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Đăng ký thành công!\nTên: $username\nEmail: $email", Toast.LENGTH_LONG).show()
        }
    }
}


