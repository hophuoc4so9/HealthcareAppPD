package com.example.healthcareapppd.presentation.ui.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.api.model.Facility
import kotlin.math.roundToInt

class FacilityAdapter(
    private val onItemClicked: (Facility) -> Unit,
    private val onItemLongClicked: (Facility) -> Unit
) : ListAdapter<Facility, FacilityAdapter.FacilityViewHolder>(FacilityDiffCallback()) {

    private var userLocation: Pair<Double, Double>? = null

    fun setUserLocation(lat: Double, lng: Double) {
        userLocation = Pair(lat, lng)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_facility, parent, false)
        return FacilityViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacilityViewHolder, position: Int) {
        val facility = getItem(position)
        holder.bind(facility, userLocation)

        // Click thường - xem trên bản đồ
        holder.itemView.setOnClickListener {
            onItemClicked(facility)
        }

        // Long click - điều hướng
        holder.itemView.setOnLongClickListener {
            onItemLongClicked(facility)
            true
        }
    }

    class FacilityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.facility_name)
        private val typeTextView: TextView = itemView.findViewById(R.id.facility_type)
        private val distanceTextView: TextView = itemView.findViewById(R.id.facility_distance)

        fun bind(facility: Facility, userLocation: Pair<Double, Double>?) {
            // Tên cơ sở
            nameTextView.text = facility.name ?: "Không có tên"

            // Loại hình
            typeTextView.text = formatType(facility.type)

            // Khoảng cách
            if (userLocation != null) {
                val distance = facility.distanceFrom(userLocation.first, userLocation.second)
                distanceTextView.text = formatDistance(distance)
                distanceTextView.visibility = View.VISIBLE
            } else {
                distanceTextView.visibility = View.GONE
            }
        }

        private fun formatType(type: String?): String {
            if (type == null) return "Cơ sở y tế"
            
            return when (type.lowercase()) {
                "hospital" -> "Bệnh viện"
                "clinic" -> "Phòng khám"
                "pharmacy" -> "Nhà thuốc"
                "dentist" -> "Nha khoa"
                "doctors" -> "Bác sĩ"
                else -> type.replaceFirstChar { it.uppercase() }
            }
        }

        private fun formatDistance(meters: Float): String {
            return when {
                meters < 1000 -> "${meters.roundToInt()} m"
                else -> "%.1f km".format(meters / 1000)
            }
        }
    }

    class FacilityDiffCallback : DiffUtil.ItemCallback<Facility>() {
        override fun areItemsTheSame(oldItem: Facility, newItem: Facility): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Facility, newItem: Facility): Boolean {
            return oldItem == newItem
        }
    }
}