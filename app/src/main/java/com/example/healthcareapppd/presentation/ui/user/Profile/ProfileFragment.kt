package com.example.healthcareapppd.presentation.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.healthcareapppd.R

class ProfileFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var menuSchedule: LinearLayout
    private lateinit var menuFaq: LinearLayout
    private lateinit var menuLogout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
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
            Toast.makeText(requireContext(), "Mở màn hình xem lịch khám", Toast.LENGTH_SHORT).show()
        }

        menuFaq.setOnClickListener {
            Toast.makeText(requireContext(), "Mở màn hình FAQ", Toast.LENGTH_SHORT).show()
        }

        // Click vào Logout
        menuLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        tvUserName.text = "Nguyễn Văn A"
        tvUserEmail.text = "nguyenvana@email.com"
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}