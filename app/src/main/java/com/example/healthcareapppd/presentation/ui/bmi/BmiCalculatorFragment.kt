package com.example.healthcareapppd.presentation.ui.bmi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.healthcareapppd.R
import com.example.healthcareapppd.databinding.FragmentBmiCalculatorBinding
import com.example.healthcareapppd.domain.usecase.Gender
import kotlinx.coroutines.launch

class BmiCalculatorFragment : Fragment() {

    // Sử dụng ViewBinding để truy cập view an toàn
    private var _binding: FragmentBmiCalculatorBinding? = null
    private val binding get() = _binding!!

    // Khởi tạo ViewModel bằng KTX delegate
    private val viewModel: BmiCalculatorViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBmiCalculatorBinding.inflate(inflater, container, false)
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
        }

        observeUiState()
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