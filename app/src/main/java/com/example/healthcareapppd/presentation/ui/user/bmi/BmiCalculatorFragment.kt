package com.example.healthcareapppd.presentation.ui.user.bmi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.healthcareapppd.R
import com.example.healthcareapppd.utils.TokenManager
import com.example.healthcareapppd.databinding.FragmentBmiCalculatorBinding
import com.example.healthcareapppd.domain.usecase.Gender
import com.example.healthcareapppd.domain.usecase.patient.AddVitalsUseCase
import kotlinx.coroutines.launch

class BmiCalculatorFragment : Fragment() {

    // Sử dụng ViewBinding để truy cập view an toàn
    private var _binding: FragmentBmiCalculatorBinding? = null
    private val binding get() = _binding!!

    // Khởi tạo ViewModel bằng KTX delegate
    private val viewModel: BmiCalculatorViewModel by viewModels()
    
    private val addVitalsUseCase = AddVitalsUseCase()
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBmiCalculatorBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCalculate.setOnClickListener {
            val weight = binding.edtWeight.text.toString()
            val height = binding.edtHeight.text.toString()
            val age = binding.edtAge.text.toString()
            val selectedGender = when (binding.rgGender.checkedRadioButtonId) {
                R.id.rbMale -> Gender.MALE
                else -> Gender.FEMALE
            }

            viewModel.calculateBmi(weight, height, age, selectedGender)
            
            // Lưu vitals lên server
            saveVitalsToServer(weight, height)
        }

        observeUiState()
    }
    
    private fun saveVitalsToServer(weight: String, height: String) {
        val token = tokenManager.getToken() ?: return
        
        val weightKg = weight.toDoubleOrNull()
        val heightCm = height.toDoubleOrNull()
        
        if (weightKg == null || heightCm == null) return
        
        viewLifecycleOwner.lifecycleScope.launch {
            val result = addVitalsUseCase(
                token = token,
                heightCm = heightCm,
                weightKg = weightKg
            )
            
            result.onSuccess {
            }.onFailure { error ->
                // Silent fail - không hiện lỗi cho user
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.resultGroup.isVisible = state.showResult

                state.result?.let { bmiResult ->
                    binding.tvBmiScore.text = bmiResult.score.toString()
                    binding.tvBmiCategory.text = bmiResult.category
                    updateCategoryTextColor(bmiResult.category)
                }
            }
        }
    }

    private fun updateCategoryTextColor(category: String) {
        val colorRes = when {
            category.contains("Thiếu cân", ignoreCase = true) -> R.color.orange
            category.contains("Bình thường", ignoreCase = true) || category.contains("khỏe mạnh", ignoreCase = true) -> R.color.teal_700
            category.contains("Thừa cân", ignoreCase = true) -> R.color.orange
            category.contains("Béo phì", ignoreCase = true) -> R.color.red
            else -> R.color.black
        }
        binding.tvBmiCategory.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Dọn dẹp binding để tránh rò rỉ bộ nhớ
    }
}