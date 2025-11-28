// File: com/example/healthcareapppd/presentation/ui/user/Docter/DocterFragment.kt
package com.example.healthcareapppd.presentation.ui.user.Docter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.api.model.DoctorProfile
import com.example.healthcareapppd.domain.usecase.doctor.GetAllDoctorsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DocterFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchView: SearchView
    private var adapter: MyDoctorAdapter? = null
    
    private val getAllDoctorsUseCase = GetAllDoctorsUseCase()
    
    private var searchJob: Job? = null
    private var allDoctors: List<DoctorProfile> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.docter_ffragment_item_list, container, false)
        
        recyclerView = view.findViewById(R.id.recycler_view_doctors)
        progressBar = view.findViewById(R.id.progressBar)
        searchView = view.findViewById(R.id.searchView)
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        setupSearchView()
        loadDoctors()
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton = view.findViewById<ImageView>(R.id.iv_back_arrow)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterDoctorsLocally(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Debounce search
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300) // Wait 300ms before searching
                    if (newText.isNullOrEmpty()) {
                        // Show all doctors when search is empty
                        setupRecyclerView(allDoctors)
                    } else {
                        filterDoctorsLocally(newText)
                    }
                }
                return true
            }
        })
        
        // Clear button
        searchView.setOnCloseListener {
            setupRecyclerView(allDoctors)
            false
        }
    }

    private fun loadDoctors() {
        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            getAllDoctorsUseCase(
                context = requireContext(),
                page = 1,
                limit = 50,
                status = null
            ).fold(
                onSuccess = { doctors ->
                    progressBar.visibility = View.GONE
                    // Filter approved doctors on client side
                    allDoctors = doctors.filter { 
                        it.status == "approved" || it.verificationStatus == "approved" 
                    }
                    
                    if (allDoctors.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Không tìm thấy bác sĩ nào",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        setupRecyclerView(allDoctors)
                    }
                },
                onFailure = { error ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Lỗi tải danh sách: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun filterDoctorsLocally(query: String) {
        val queryLower = query.lowercase().trim()
        val filteredDoctors = allDoctors.filter { doctor ->
            doctor.fullName.lowercase().contains(queryLower) ||
            doctor.specialization.lowercase().contains(queryLower) ||
            doctor.clinicAddress?.lowercase()?.contains(queryLower) == true ||
            doctor.bio?.lowercase()?.contains(queryLower) == true
        }
        
        setupRecyclerView(filteredDoctors)
        
        if (filteredDoctors.isEmpty() && query.isNotEmpty()) {
            Toast.makeText(
                requireContext(),
                "Không tìm thấy bác sĩ nào với từ khóa \"$query\"",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupRecyclerView(doctors: List<DoctorProfile>) {
        adapter = MyDoctorAdapter(doctors) { doctor ->
            // Chuyển sang màn hình chi tiết bác sĩ, truyền dữ liệu doctor qua Bundle
            val bundle = Bundle().apply {
                putSerializable("KEY_DOCTOR", doctor)
            }
            findNavController().navigate(R.id.action_docter_to_detail, bundle)
        }
        recyclerView.adapter = adapter
    }
}