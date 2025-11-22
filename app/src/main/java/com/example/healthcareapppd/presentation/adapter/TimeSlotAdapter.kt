package com.example.healthcareapppd.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.api.model.AvailabilitySlot
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class TimeSlotAdapter(
    private var timeSlots: List<AvailabilitySlot>,
    private val onSlotSelected: (AvailabilitySlot) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    private var selectedPosition = -1

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardTimeSlot: MaterialCardView = itemView.findViewById(R.id.card_time_slot)
        val tvTimeSlot: TextView = itemView.findViewById(R.id.tv_time_slot)

        fun bind(slot: AvailabilitySlot, position: Int) {
            // Format time display
            val timeText = formatTimeRange(slot.startTime, slot.endTime)
            tvTimeSlot.text = timeText

            // Update selection state
            cardTimeSlot.isChecked = position == selectedPosition
            
            // Set colors based on selection
            if (position == selectedPosition) {
                cardTimeSlot.setCardBackgroundColor(itemView.context.getColor(R.color.primary_blue))
                tvTimeSlot.setTextColor(itemView.context.getColor(android.R.color.white))
            } else {
                cardTimeSlot.setCardBackgroundColor(itemView.context.getColor(android.R.color.white))
                tvTimeSlot.setTextColor(itemView.context.getColor(android.R.color.black))
            }

            cardTimeSlot.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onSlotSelected(slot)
            }
        }

        private fun formatTimeRange(startTime: String, endTime: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale("vi"))
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                
                val outputFormat = SimpleDateFormat("HH:mm", Locale("vi"))
                
                val startDate = inputFormat.parse(startTime)
                val endDate = inputFormat.parse(endTime)
                
                val startFormatted = startDate?.let { outputFormat.format(it) } ?: ""
                val endFormatted = endDate?.let { outputFormat.format(it) } ?: ""
                
                "$startFormatted - $endFormatted"
            } catch (e: Exception) {
                "Không xác định"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(timeSlots[position], position)
    }

    override fun getItemCount(): Int = timeSlots.size

    fun updateTimeSlots(newSlots: List<AvailabilitySlot>) {
        // Sắp xếp theo thời gian từ sớm đến muộn
        timeSlots = newSlots.sortedBy { it.startTime }
        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun getSelectedSlot(): AvailabilitySlot? {
        return if (selectedPosition != -1) timeSlots[selectedPosition] else null
    }

    fun clearSelection() {
        val previousPosition = selectedPosition
        selectedPosition = -1
        notifyItemChanged(previousPosition)
    }
}
