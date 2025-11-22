package com.example.healthcareapppd.presentation.ui.user.Profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.healthcareapppd.R
import com.example.healthcareapppd.utils.TokenManager
import kotlinx.coroutines.launch
import java.util.*

class CreatePatientProfileFragment : Fragment() {

    private val viewModel: PatientProfileViewModel by viewModels()
    
    private lateinit var etFullName: EditText
    private lateinit var etDateOfBirth: EditText
    private lateinit var spinnerSex: Spinner
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var btnCreateProfile: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvSuccess: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_patient_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupSexSpinner()
        setupDatePicker()
        setupClickListeners()
        observeUIState()
    }

    private fun initViews(view: View) {
        etFullName = view.findViewById(R.id.etFullName)
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth)
        spinnerSex = view.findViewById(R.id.spinnerSex)
        etPhone = view.findViewById(R.id.etPhone)
        etAddress = view.findViewById(R.id.etAddress)
        btnCreateProfile = view.findViewById(R.id.btnCreateProfile)
        progressBar = view.findViewById(R.id.progressBar)
        tvError = view.findViewById(R.id.tvError)
        tvSuccess = view.findViewById(R.id.tvSuccess)
    }

    private fun setupSexSpinner() {
        val sexOptions = arrayOf("Nam", "Nữ", "Khác", "Không muốn nói")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sexOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSex.adapter = adapter
    }

    private fun setupDatePicker() {
        etDateOfBirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    etDateOfBirth.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
        etDateOfBirth.isFocusable = false
    }

    private fun setupClickListeners() {
        btnCreateProfile.setOnClickListener {
            if (validateForm()) {
                createProfile()
            }
        }
    }

    private fun validateForm(): Boolean {
        val fullName = etFullName.text.toString().trim()
        val dateOfBirth = etDateOfBirth.text.toString().trim()

        return when {
            fullName.isEmpty() -> {
                showError("Vui lòng nhập họ và tên")
                false
            }
            dateOfBirth.isEmpty() -> {
                showError("Vui lòng chọn ngày sinh")
                false
            }
            else -> true
        }
    }

    private fun createProfile() {
        val token = TokenManager.getToken(requireContext()) ?: return
        val fullName = etFullName.text.toString().trim()
        val dateOfBirth = etDateOfBirth.text.toString().trim()
        val sexValues = arrayOf("male", "female", "other", "prefer_not_to_say")
        val sex = sexValues[spinnerSex.selectedItemPosition]
        val phoneNumber = etPhone.text.toString().trim().ifEmpty { null }
        val address = etAddress.text.toString().trim().ifEmpty { null }

        Log.d("CreateProfile", "📝 Creating profile with:")
        Log.d("CreateProfile", "   fullName: '$fullName'")
        Log.d("CreateProfile", "   dateOfBirth: '$dateOfBirth'")
        Log.d("CreateProfile", "   sex: '$sex'")
        Log.d("CreateProfile", "   phoneNumber: '$phoneNumber'")
        Log.d("CreateProfile", "   address: '$address'")

        viewModel.createProfile(
            token, fullName, dateOfBirth, sex, phoneNumber, address,
            null, null
        )
    }

    private fun observeUIState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                state.error?.let { error ->
                    showError(error)
                    viewModel.clearMessages()
                }
                
                state.success?.let { success ->
                    showSuccess(success)
                    clearForm()
                    viewModel.clearMessages()
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun showError(message: String) {
        tvError.visibility = View.VISIBLE
        tvError.text = message
        tvSuccess.visibility = View.GONE
    }

    private fun showSuccess(message: String) {
        tvSuccess.visibility = View.VISIBLE
        tvSuccess.text = message
        tvError.visibility = View.GONE
    }

    private fun clearForm() {
        etFullName.text.clear()
        etDateOfBirth.text.clear()
        spinnerSex.setSelection(0)
        etPhone.text.clear()
        etAddress.text.clear()
    }

    companion object {
        @JvmStatic
        fun newInstance() = CreatePatientProfileFragment()
    }
}
