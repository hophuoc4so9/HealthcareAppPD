package com.example.healthcareapppd.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.healthcareapppd.R
import com.example.healthcareapppd.WelcomeActivity

class DoctorProfileFragment : Fragment() {
    private lateinit var btnLogout: Button

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        Log.d("DoctorProfileFragment", "onAttach called")
        Toast.makeText(context, "onAttach called", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DoctorProfileFragment", "onCreate called")
        Toast.makeText(requireContext(), "onCreate called", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("DoctorProfileFragment", "onCreateView called")
        Toast.makeText(requireContext(), "onCreateView called", Toast.LENGTH_SHORT).show()
        val view = inflater.inflate(R.layout.fragment_doctor_profile, container, false)
        Log.d("DoctorProfileFragment", "fragment_doctor_profile inflated: ${view != null}")
        btnLogout = view.findViewById(R.id.btnLogout)
        Log.d("DoctorProfileFragment", "btnLogout found: ${btnLogout != null}")
        btnLogout.setOnClickListener {
            Log.d("DoctorProfileFragment", "Logout button clicked")
            Toast.makeText(requireContext(), "Bạn vừa nhấn Đăng xuất", Toast.LENGTH_SHORT).show()
            logout()
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d("DoctorProfileFragment", "onActivityCreated called")
        Toast.makeText(requireContext(), "onActivityCreated called", Toast.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DoctorProfileFragment", "onViewCreated called")
        Toast.makeText(requireContext(), "onViewCreated called", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        Log.d("DoctorProfileFragment", "onStart called")
        Toast.makeText(requireContext(), "onStart called", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Log.d("DoctorProfileFragment", "onResume called")
        Toast.makeText(requireContext(), "onResume called", Toast.LENGTH_SHORT).show()
    }

    private fun logout() {
        Log.d("DoctorProfileFragment", "logout() called")
        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
        Toast.makeText(requireContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finishAffinity()
    }
}
