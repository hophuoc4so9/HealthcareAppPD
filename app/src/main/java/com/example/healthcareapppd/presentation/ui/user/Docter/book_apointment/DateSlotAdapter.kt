package com.example.healthcareapppd.presentation.ui.user.Docter.book_apointment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.domain.usecase.DateSlot

class DateSlotAdapter(
    private val onDateSelected: (DateSlot) -> Unit // Callback khi ngày được chọn
) : RecyclerView.Adapter<DateSlotAdapter.DateSlotViewHolder>() {

    private val dateList = mutableListOf<DateSlot>()
    private var selectedPosition = RecyclerView.NO_POSITION

    inner class DateSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.ll_date_slot_container)
        val tvDayOfWeek: TextView = itemView.findViewById(R.id.tv_day_of_week)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)

        fun bind(dateSlot: DateSlot) {
            tvDayOfWeek.text = dateSlot.dayOfWeek
            tvDate.text = dateSlot.date

            // Áp dụng trạng thái isSelected cho View Container
            container.isSelected = dateSlot.isSelected
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_date_slot, parent, false)
        return DateSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateSlotViewHolder, position: Int) {
        val dateSlot = dateList[position]
        holder.bind(dateSlot)

        // Xử lý sự kiện click
        holder.container.setOnClickListener {
            if (selectedPosition != RecyclerView.NO_POSITION) {
                dateList[selectedPosition].isSelected = false
                notifyItemChanged(selectedPosition)
            }

            // Cập nhật trạng thái của item MỚI
            selectedPosition = holder.bindingAdapterPosition
            dateList[selectedPosition].isSelected = true
            notifyItemChanged(selectedPosition)

            // Gọi Callback để Fragment/ViewModel xử lý logic nghiệp vụ
            onDateSelected(dateSlot)
        }
    }

    override fun getItemCount(): Int = dateList.size

    // Hàm public để ViewModel đổ dữ liệu vào
    fun submitList(newDates: List<DateSlot>) {
        dateList.clear()
        dateList.addAll(newDates)
        // Reset selected position khi dữ liệu mới được tải
        selectedPosition = RecyclerView.NO_POSITION

        notifyDataSetChanged()
    }
}