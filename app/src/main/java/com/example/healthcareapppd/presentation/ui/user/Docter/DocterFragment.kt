package com.example.healthcareapppd.presentation.ui.user.Docter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
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

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }
    private fun createSampleData(): List<DoctorUsecase> {
        return listOf(
            DoctorUsecase(R.drawable.ic_doctor, "Dr. Rishi", "Cardiologist", 4.7f, "800m away"),
            DoctorUsecase(R.drawable.ic_doctor, "Dr. Vaamana", "Dentist", 4.7f, "800m away"),
            DoctorUsecase(R.drawable.ic_doctor, "Dr. Nallarasi", "Orthopaedic", 4.7f, "800m away")
            // Thêm các bác sĩ khác nếu bạn muốn
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.docter_ffragment_item_list, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_doctors)

        if (recyclerView is RecyclerView) {
            with(recyclerView) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = MyDoctorAdapter(createSampleData())            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = view.findViewById<ImageView>(R.id.iv_back_arrow)

        backButton?.setOnClickListener {
            findNavController().navigate(R.id.action_docter_to_home)
        }
    }

    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            DocterFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

}