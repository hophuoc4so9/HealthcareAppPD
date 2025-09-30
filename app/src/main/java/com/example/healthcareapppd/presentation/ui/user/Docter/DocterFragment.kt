// File: com/example/healthcareapppd/presentation/ui/user/Docter/DocterFragment.kt
package com.example.healthcareapppd.presentation.ui.user.Docter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.example.healthcareapppd.R
import com.example.healthcareapppd.domain.usecase.DoctorUsecase

class DocterFragment : Fragment() {

    private fun createSampleData(): List<DoctorUsecase> {
        // Bạn có thể thay bằng dữ liệu thật
        return listOf(
            DoctorUsecase(R.drawable.ic_doctor, "Dr. A", "Bác sỹ", 4.7f, "800m away"),
            DoctorUsecase(R.drawable.ic_doctor, "Dr. B", "Nha khoa", 4.7f, "800m away"),
            DoctorUsecase(R.drawable.ic_doctor, "Dr. C", "Nha khoa", 4.7f, "800m away")
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.docter_ffragment_item_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_doctors)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = MyDoctorAdapter(createSampleData()) { doctor ->
            // Khi một bác sĩ được click
            val detailFragment = DetailDocterFragment()

            // Dùng Bundle để gói dữ liệu
            val bundle = Bundle()
            bundle.putSerializable("KEY_DOCTOR", doctor)
            detailFragment.arguments = bundle

            findNavController().navigate(R.id.action_docter_to_detail)

        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton = view.findViewById<ImageView>(R.id.iv_back_arrow)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}