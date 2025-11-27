package com.example.healthcareapppd.presentation.ui.user.bmi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.repository.GeminiBmiRepository
import com.example.healthcareapppd.databinding.FragmentBmiCalculatorBinding
import com.example.healthcareapppd.domain.usecase.CalculateBmiUseCase
import com.example.healthcareapppd.domain.usecase.Gender
import com.example.healthcareapppd.domain.usecase.patient.AddVitalsUseCase
import com.example.healthcareapppd.utils.TokenManager
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch

class BmiCalculatorFragment : Fragment() {

    private var _binding: FragmentBmiCalculatorBinding? = null
    private val binding get() = _binding!!

    // --- KHỞI TẠO CÁC DEPENDENCY ---
    // 1. Tạo Repository (Thay API Key của bạn vào đây)
    // Tốt nhất nên để API Key trong local.properties hoặc BuildConfig
    private val bmiAiRepository by lazy {
        GeminiBmiRepository("AIzaSyCSMz-daEk0gRyZ3mnpglmLao1IJtKRLss")
    }

    // 2. Tạo UseCase với Repository
    private val calculateBmiUseCase by lazy {
        CalculateBmiUseCase(bmiAiRepository)
    }

    // 3. Khởi tạo ViewModel thông qua Factory
    private val viewModel: BmiCalculatorViewModel by viewModels {
        BmiViewModelFactory(calculateBmiUseCase)
    }

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

            // Validate sơ bộ UI
            if (weight.isBlank() || height.isBlank() || age.isBlank()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedGender = when (binding.rgGender.checkedRadioButtonId) {
                R.id.rbMale -> Gender.MALE
                else -> Gender.FEMALE
            }

            // Gọi ViewModel tính toán
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
            result.onFailure {
                // Silent fail
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // 1. Hiển thị thông báo lỗi nếu có
                    if (state.error != null) {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }

                    // 2. Ẩn/Hiện layout kết quả
                    binding.resultGroup.isVisible = state.showResult

                    if (state.showResult) {
                        // Cập nhật số liệu BMI (Offline có ngay)
                        binding.tvBmiScore.text = String.format("%.1f", state.bmi)
                        binding.tvBmiCategory.text = state.category
                        updateCategoryTextColor(state.category)

                        // Cập nhật lời khuyên
                        val markwon = Markwon.create(requireContext())
                        markwon.setMarkdown(binding.tvAdvice, state.advice)


                    }
                }
            }
        }
    }

    private fun updateCategoryTextColor(category: String) {
        val colorRes = when {
            category.contains("Thiếu cân", ignoreCase = true) -> R.color.orange
            category.contains("Bình thường", ignoreCase = true) || category.contains("Khỏe mạnh", ignoreCase = true) -> R.color.teal_700
            category.contains("Thừa cân", ignoreCase = true) -> R.color.orange
            category.contains("Béo phì", ignoreCase = true) -> R.color.red
            else -> R.color.black
        }
        binding.tvBmiCategory.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}