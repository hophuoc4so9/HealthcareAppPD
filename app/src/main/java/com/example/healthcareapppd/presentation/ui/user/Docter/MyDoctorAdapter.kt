// File: com/example/healthcareapppd/presentation/ui/user/Docter/MyDoctorAdapter.kt
package com.example.healthcareapppd.presentation.ui.user.Docter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.domain.usecase.DoctorUsecase

class MyDoctorAdapter(
    private val doctors: List<DoctorUsecase>,
    private val onDoctorClicked: (DoctorUsecase) -> Unit // Callback để Fragment xử lý
) : RecyclerView.Adapter<MyDoctorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.docter_fragment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doctor = doctors[position]
        holder.bind(doctor)
    }

    override fun getItemCount(): Int = doctors.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photo: ImageView = itemView.findViewById(R.id.iv_doctor_photo)
        private val name: TextView = itemView.findViewById(R.id.tv_doctor_name)
        private val speciality: TextView = itemView.findViewById(R.id.tv_doctor_speciality)
        private val rating: TextView = itemView.findViewById(R.id.tv_doctor_rating)
        private val distance: TextView = itemView.findViewById(R.id.tv_doctor_distance)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDoctorClicked(doctors[position])
                }
            }
        }

        fun bind(doctor: DoctorUsecase) {
            photo.setImageResource(doctor.photo)
            name.text = doctor.name
            speciality.text = doctor.speciality
            rating.text = doctor.rating.toString()
            distance.text = doctor.distance
        }
    }
}