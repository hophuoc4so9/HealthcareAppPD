package com.example.healthcareapppd.presentation.ui.user.Home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.healthcareapppd.R
// SỬA LẠI DÒNG NÀY: Dùng binding của Home, không phải của Report
import com.example.healthcareapppd.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.layoutDoctor.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_docter)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}