// File: com/example/healthcareapppd/presentation/ui/user/Docter/DetailDocterFragment.kt
package com.example.healthcareapppd.presentation.ui.user.Docter

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.domain.usecase.DoctorUsecase

class DetailDocterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_docter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lấy dữ liệu từ arguments
        val doctor = arguments?.getSerializable("KEY_DOCTOR") as? DoctorUsecase

        // Tìm tất cả các view cần thiết
        val photo: ImageView = view.findViewById(R.id.iv_doctor_photo)
        val name: TextView = view.findViewById(R.id.tv_doctor_name)
        val speciality: TextView = view.findViewById(R.id.tv_doctor_speciality)
        val rating: TextView = view.findViewById(R.id.tv_doctor_rating)
        val distance: TextView = view.findViewById(R.id.tv_doctor_distance)
        val doctorInfoDesc: TextView = view.findViewById(R.id.tv_doctor_info_desc)
        val backButton: ImageView = view.findViewById(R.id.iv_back_arrow)

        if (doctor != null) {
            // TRƯỜNG HỢP 1: CÓ DỮ LIỆU THẬT
            Log.d("DetailFragment", "Doctor data found: ${doctor.name}")

            photo.setImageResource(doctor.photo)
            name.text = doctor.name
            speciality.text = doctor.speciality
            rating.text = doctor.rating.toString()
            distance.text = doctor.distance
            doctorInfoDesc.text = "Bác sĩ ${doctor.name} là một chuyên gia hàng đầu trong lĩnh vực ${doctor.speciality}. Với hơn 10 năm kinh nghiệm, bác sĩ đã điều trị thành công cho hàng ngàn bệnh nhân."

        } else {
            // MỚI: TRƯỜNG HỢP 2: KHÔNG CÓ DỮ LIỆU (doctor bị null)
            Log.e("DetailFragment", "Doctor data is NULL. Displaying temporary data.")

            // Tạo một đối tượng bác sĩ tạm thời để hiển thị
            val tmpDoctor = DoctorUsecase(
                R.drawable.ic_doctor, // Dùng một icon mặc định
                "Bác sĩ A",
                "Chuyên Khoa Chung",
                4.5f,
                "Không xác định"
            )

            // Gán dữ liệu tạm thời lên giao diện
            photo.setImageResource(tmpDoctor.photo)
            name.text = tmpDoctor.name
            speciality.text = tmpDoctor.speciality
            rating.text = tmpDoctor.rating.toString()
            distance.text = tmpDoctor.distance
            doctorInfoDesc.text = "Thông tin chi tiết của bác sĩ. Vui lòng thử lại sau. Đây là thông tin mẫu."
        }

        // Xử lý nút quay lại
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}