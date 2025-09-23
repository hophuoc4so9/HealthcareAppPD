package com.example.healthcareapppd.presentation.ui.Docter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.databinding.DocterFragmentItemBinding
import com.example.healthcareapppd.domain.usecase.DoctorUsecase

class MyDoctorAdapter(
    private val doctorList: List<DoctorUsecase>
) : RecyclerView.Adapter<MyDoctorAdapter.DoctorViewHolder>() {

    inner class DoctorViewHolder(binding: DocterFragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val doctorImage: ImageView = binding.ivDoctorPhoto
        val doctorName: TextView = binding.tvDoctorName
        val doctorSpecialty: TextView = binding.tvDoctorSpeciality
        val doctorRating: TextView = binding.tvDoctorRating
        val doctorDistance: TextView = binding.tvDoctorDistance
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val binding = DocterFragmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DoctorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val currentDoctor = doctorList[position]

        holder.doctorImage.setImageResource(currentDoctor.image)
        holder.doctorName.text = currentDoctor.name
        holder.doctorSpecialty.text = currentDoctor.specialty
        holder.doctorRating.text = currentDoctor.rating.toString()
        holder.doctorDistance.text = currentDoctor.distance
    }

    override fun getItemCount(): Int {
        return doctorList.size
    }
}