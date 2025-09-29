package com.example.healthcareapppd.presentation.ui.reminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.google.android.material.switchmaterial.SwitchMaterial

data class Reminder(
    val time: String,     // "07:00"
    val label: String,    // "Uống thuốc"
    var isActive: Boolean // true/false
)


class ReminderAdapter(
    private val reminders: MutableList<Reminder>
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvLabel: TextView = itemView.findViewById(R.id.tvLabel)
        val switchActive: SwitchMaterial = itemView.findViewById(R.id.switchActive)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reminder_fragment_item, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.tvTime.text = reminder.time
        holder.tvLabel.text = reminder.label
        holder.switchActive.isChecked = reminder.isActive

        // Cập nhật trạng thái khi bật/tắt Switch
        holder.switchActive.setOnCheckedChangeListener(null)
        holder.switchActive.isChecked = reminder.isActive
        holder.switchActive.setOnCheckedChangeListener { _, isChecked ->
            reminder.isActive = isChecked
        }
    }

    override fun getItemCount(): Int = reminders.size

    // Hàm thêm nhắc nhở mới
    fun addReminder(reminder: Reminder) {
        reminders.add(reminder)
        notifyItemInserted(reminders.size - 1)
    }
}
