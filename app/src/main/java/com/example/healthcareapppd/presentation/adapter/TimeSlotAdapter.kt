package com.example.healthcareapppd.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
        private val cardTimeSlot: MaterialCardView = itemView.findViewById(R.id.card_time_slot)
        private val tvTimeSlot: TextView = itemView.findViewById(R.id.tv_time_slot)

        fun bind(slot: AvailabilitySlot, position: Int) {
            // 1. Hiển thị giờ
            tvTimeSlot.text = formatTimeRange(slot.startTime, slot.endTime)

            // 2. Xử lý màu sắc khi được chọn
            if (position == selectedPosition) {
                cardTimeSlot.setCardBackgroundColor(itemView.context.getColor(R.color.primary_blue))
                tvTimeSlot.setTextColor(itemView.context.getColor(android.R.color.white))
                cardTimeSlot.isChecked = true
                cardTimeSlot.strokeWidth = 0
            } else {
                cardTimeSlot.setCardBackgroundColor(itemView.context.getColor(android.R.color.white))
                tvTimeSlot.setTextColor(itemView.context.getColor(android.R.color.black))
                cardTimeSlot.isChecked = false
                cardTimeSlot.strokeWidth = 2 // Thêm viền nhẹ nếu muốn card chưa chọn nổi hơn
            }

            // 3. Xử lý sự kiện click
            cardTimeSlot.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                val previous = selectedPosition
                selectedPosition = currentPosition

                if (previous != -1) notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)

                onSlotSelected(slot)
            }
        }

        private fun formatTimeRange(start: String, end: String): String {
            val startTime = formatToHourMinute(start)
            val endTime = formatToHourMinute(end)
            return "$startTime - $endTime"
        }

        private fun formatToHourMinute(isoDate: String?): String {
            if (isoDate == null) return "??:??"
            return try {
                val formats = listOf(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss"
                )
                var date: Date? = null
                

                for (fmt in formats) {
                    try {
                        val inputFormat = SimpleDateFormat(fmt, Locale.US)
                        inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Input là UTC
                        date = inputFormat.parse(isoDate)
                        if (date != null) break
                    } catch (_: Exception) {}
                }

                val outputFormat = SimpleDateFormat("HH:mm", Locale.US)
                outputFormat.timeZone = TimeZone.getTimeZone("UTC") // Output cũng là UTC
                
                date?.let { outputFormat.format(it) } ?: "??:??"
            } catch (e: Exception) {
                try {
                    // Ví dụ: 2025-11-25T08:00:00... -> Lấy từ ký tự T+1 đến T+6
                    if (isoDate.contains("T")) {
                        val timePart = isoDate.split("T")[1]
                        if (timePart.length >= 5) timePart.substring(0, 5) else "??:??"
                    } else "??:??"
                } catch (ex: Exception) {
                    "??:??"
                }
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
        // Sort danh sách theo thời gian tăng dần
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

        
        timeSlots = newSlots.sortedBy {
            it.startTime 
        }

        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun getSelectedSlot(): AvailabilitySlot? {
        return if (selectedPosition != -1 && selectedPosition < timeSlots.size) {
            timeSlots[selectedPosition]
        } else {
            null
        }
    }

    fun clearSelection() {
        val previous = selectedPosition
        selectedPosition = -1
        if (previous != -1) notifyItemChanged(previous)
    }
}