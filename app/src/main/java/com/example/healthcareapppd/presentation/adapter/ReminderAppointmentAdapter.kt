package com.example.healthcareapppd.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.api.model.Appointment
import com.example.healthcareapppd.data.api.model.Reminder
import com.google.android.material.chip.Chip
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.SimpleDateFormat
import java.util.*

sealed class ReminderAppointmentItem {
    data class AppointmentItem(val appointment: Appointment) : ReminderAppointmentItem()
    data class ReminderItem(val reminder: Reminder) : ReminderAppointmentItem()
}

class ReminderAppointmentAdapter(
    private val items: MutableList<ReminderAppointmentItem> = mutableListOf(),
    private val onReminderToggle: (Reminder, Boolean) -> Unit,
    private val onReminderDelete: ((Reminder) -> Unit)? = null,
    private val onAppointmentClick: ((Appointment) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_APPOINTMENT = 0
        private const val VIEW_TYPE_REMINDER = 1
    }

    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDoctorName: TextView = itemView.findViewById(R.id.tv_doctor_name)
        val tvSpecialization: TextView = itemView.findViewById(R.id.tv_specialization)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val tvPatientNotes: TextView = itemView.findViewById(R.id.tv_patient_notes)
        val chipStatus: Chip = itemView.findViewById(R.id.chip_status)
    }

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvLabel: TextView = itemView.findViewById(R.id.tvLabel)
        val switchActive: SwitchMaterial = itemView.findViewById(R.id.switchActive)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ReminderAppointmentItem.AppointmentItem -> VIEW_TYPE_APPOINTMENT
            is ReminderAppointmentItem.ReminderItem -> VIEW_TYPE_REMINDER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_APPOINTMENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_appointment, parent, false)
                AppointmentViewHolder(view)
            }
            VIEW_TYPE_REMINDER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.reminder_fragment_item, parent, false)
                ReminderViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ReminderAppointmentItem.AppointmentItem -> {
                bindAppointment(holder as AppointmentViewHolder, item.appointment)
            }
            is ReminderAppointmentItem.ReminderItem -> {
                bindReminder(holder as ReminderViewHolder, item.reminder)
            }
        }
    }

    private fun bindAppointment(holder: AppointmentViewHolder, appointment: Appointment) {
        holder.tvDoctorName.text = appointment.doctorName ?: "Bác sĩ"
        holder.tvSpecialization.text = appointment.specialization ?: ""
        
        // Format time
        val timeText = formatAppointmentTime(appointment.startTime, appointment.endTime)
        holder.tvTime.text = timeText
        
        // Patient notes
        if (!appointment.patientNotes.isNullOrBlank()) {
            holder.tvPatientNotes.visibility = View.VISIBLE
            holder.tvPatientNotes.text = "Ghi chú: ${appointment.patientNotes}"
        } else {
            holder.tvPatientNotes.visibility = View.GONE
        }
        
        // Status chip
        when (appointment.status) {
            "scheduled" -> {
                holder.chipStatus.text = "Đã đặt"
                holder.chipStatus.setChipBackgroundColorResource(R.color.green_light)
            }
            "completed" -> {
                holder.chipStatus.text = "Hoàn thành"
                holder.chipStatus.setChipBackgroundColorResource(R.color.blue_light)
            }
            "cancelled" -> {
                holder.chipStatus.text = "Đã hủy"
                holder.chipStatus.setChipBackgroundColorResource(R.color.red_light)
            }
            else -> {
                holder.chipStatus.text = appointment.status
                holder.chipStatus.setChipBackgroundColorResource(R.color.gray_light)
            }
        }
        
        holder.itemView.setOnClickListener {
            onAppointmentClick?.invoke(appointment)
        }
    }

    private fun bindReminder(holder: ReminderViewHolder, reminder: Reminder) {
        holder.tvLabel.text = reminder.title
        
        // Display time
        holder.tvTime.text = when {
            reminder.cronExpression != null -> parseCronExpression(reminder.cronExpression)
            reminder.oneTimeAt != null -> parseOneTimeAt(reminder.oneTimeAt)
            else -> "Không rõ thời gian"
        }
        
        // Set switch state
        holder.switchActive.setOnCheckedChangeListener(null)
        holder.switchActive.isChecked = reminder.isActive
        
        holder.switchActive.setOnCheckedChangeListener { _, isChecked ->
            onReminderToggle(reminder, isChecked)
        }
        
        onReminderDelete?.let { deleteCallback ->
            holder.itemView.setOnLongClickListener {
                deleteCallback(reminder)
                true
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(appointments: List<Appointment>, reminders: List<Reminder>) {
        items.clear()
        
        // Combine and sort by time
        val combinedItems = mutableListOf<ReminderAppointmentItem>()
        
        appointments.forEach { appointment ->
            combinedItems.add(ReminderAppointmentItem.AppointmentItem(appointment))
        }
        
        reminders.forEach { reminder ->
            combinedItems.add(ReminderAppointmentItem.ReminderItem(reminder))
        }
        
        // Sort by time (appointments by startTime, reminders by oneTimeAt or current time)
        combinedItems.sortByDescending { item ->
            when (item) {
                is ReminderAppointmentItem.AppointmentItem -> {
                    parseIsoToTimestamp(item.appointment.startTime)
                }
                is ReminderAppointmentItem.ReminderItem -> {
                    parseIsoToTimestamp(item.reminder.oneTimeAt)
                }
            }
        }
        
        items.addAll(combinedItems)
        notifyDataSetChanged()
    }
    
    fun removeReminder(reminder: Reminder) {
        val index = items.indexOfFirst { 
            it is ReminderAppointmentItem.ReminderItem && it.reminder.id == reminder.id 
        }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }
    
    fun updateReminder(reminder: Reminder) {
        val index = items.indexOfFirst { 
            it is ReminderAppointmentItem.ReminderItem && it.reminder.id == reminder.id 
        }
        if (index != -1) {
            items[index] = ReminderAppointmentItem.ReminderItem(reminder)
            notifyItemChanged(index)
        }
    }

    private fun formatAppointmentTime(startTime: String?, endTime: String?): String {
        if (startTime == null) return "Chưa xác định"
        
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            
            val startDate = inputFormat.parse(startTime)
            val endDate = endTime?.let { inputFormat.parse(it) }
            
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            if (startDate != null) {
                val startTimeStr = timeFormat.format(startDate)
                val endTimeStr = endDate?.let { timeFormat.format(it) } ?: ""
                val dateStr = dateFormat.format(startDate)
                
                if (endTimeStr.isNotEmpty()) {
                    "$startTimeStr - $endTimeStr, $dateStr"
                } else {
                    "$startTimeStr, $dateStr"
                }
            } else {
                startTime
            }
        } catch (e: Exception) {
            startTime
        }
    }
    
    private fun parseCronExpression(cron: String): String {
        val parts = cron.split(" ")
        if (parts.size >= 2) {
            val minute = parts[0].padStart(2, '0')
            val hour = parts[1].padStart(2, '0')
            return "$hour:$minute"
        }
        return cron
    }
    
    private fun parseOneTimeAt(isoDate: String?): String {
        if (isoDate == null) return "Không rõ"
        
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(isoDate)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: isoDate
        } catch (e: Exception) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(isoDate)
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                date?.let { outputFormat.format(it) } ?: isoDate
            } catch (e2: Exception) {
                isoDate
            }
        }
    }
    
    private fun parseIsoToTimestamp(isoDate: String?): Long {
        if (isoDate == null) return 0L
        
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(isoDate)
            date?.time ?: 0L
        } catch (e: Exception) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(isoDate)
                date?.time ?: 0L
            } catch (e2: Exception) {
                0L
            }
        }
    }
}
