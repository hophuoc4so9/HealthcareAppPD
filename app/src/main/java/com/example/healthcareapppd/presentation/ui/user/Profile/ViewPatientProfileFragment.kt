package com.example.healthcareapppd.presentation.ui.user.Profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.healthcareapppd.R
import com.example.healthcareapppd.utils.TokenManager
import kotlinx.coroutines.launch

class ViewPatientProfileFragment : Fragment() {

    private val viewModel: PatientProfileViewModel by viewModels()
    
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var containerProfile: LinearLayout
    private lateinit var containerEmpty: LinearLayout
    
    private lateinit var tvFullName: TextView
    private lateinit var tvDateOfBirth: TextView
    private lateinit var tvSex: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAddress: TextView
    
    private lateinit var btnEditProfile: Button
    private lateinit var btnCreateProfile: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_patient_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        loadProfile()
        observeUIState()
    }

    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progressBar)
        tvError = view.findViewById(R.id.tvError)
        containerProfile = view.findViewById(R.id.containerProfile)
        containerEmpty = view.findViewById(R.id.containerEmpty)
        
        tvFullName = view.findViewById(R.id.tvFullName)
        tvDateOfBirth = view.findViewById(R.id.tvDateOfBirth)
        tvSex = view.findViewById(R.id.tvSex)
        tvPhone = view.findViewById(R.id.tvPhone)
        tvAddress = view.findViewById(R.id.tvAddress)
        
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnCreateProfile = view.findViewById(R.id.btnCreateProfile)
        
        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_viewPatientProfileFragment_to_editPatientProfileFragment)
        }
        
        btnCreateProfile.setOnClickListener {
            findNavController().navigate(R.id.action_viewPatientProfileFragment_to_createPatientProfileFragment)
        }
    }

    private fun loadProfile() {
        val token = TokenManager.getToken(requireContext()) ?: return
        viewModel.getMyProfile(token)
    }

    private fun observeUIState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                state.error?.let { error ->
                    tvError.visibility = View.VISIBLE
                    tvError.text = error
                    containerProfile.visibility = View.GONE
                    containerEmpty.visibility = View.VISIBLE
                    
                    // Ẩn nút edit khi không có hồ sơ
                    btnEditProfile.visibility = View.GONE
                    btnCreateProfile.visibility = View.VISIBLE
                }
                
                state.profile?.let { profile ->
                    tvError.visibility = View.GONE
                    containerProfile.visibility = View.VISIBLE
                    containerEmpty.visibility = View.GONE
                    
                    // Hiển thị nút edit, ẩn nút tạo
                    btnEditProfile.visibility = View.VISIBLE
                    btnCreateProfile.visibility = View.GONE
                    
                    displayProfile(profile)
                }
            }
        }
    }

    private fun displayProfile(profile: com.example.healthcareapppd.data.api.model.PatientProfile) {
        tvFullName.text = "Họ và tên: ${profile.fullName}"
        tvDateOfBirth.text = "Ngày sinh: ${formatDateOfBirth(profile.dateOfBirth)}"
        tvSex.text = "Giới tính: ${getSexDisplay(profile.sex)}"
        tvPhone.text = "Số điện thoại: ${profile.phoneNumber ?: "Chưa cập nhật"}"
        tvAddress.text = "Địa chỉ: ${profile.address ?: "Chưa cập nhật"}"
    }

    private fun getSexDisplay(sex: String): String {
        return when (sex) {
            "male" -> "Nam"
            "female" -> "Nữ"
            "other" -> "Khác"
            "prefer_not_to_say" -> "Không muốn nói"
            else -> sex
        }
    }

    private fun formatDateOfBirth(dateStr: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            date?.let { outputFormat.format(it) } ?: dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date ?: "")
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun formatTimestamp(dateStr: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date ?: "")
        } catch (e: Exception) {
            dateStr
        }
    }

    override fun onResume() {
        super.onResume()
        // Tải lại hồ sơ khi quay lại fragment
        loadProfile()
    }

    companion object {
        @JvmStatic
        fun newInstance() = ViewPatientProfileFragment()
    }
}
