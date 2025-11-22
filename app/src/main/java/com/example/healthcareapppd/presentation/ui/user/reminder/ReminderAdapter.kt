package com.example.healthcareapppd.presentation.ui.reminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.api.model.Reminder
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.SimpleDateFormat
import java.util.*

class ReminderAdapter(
    private val reminders: MutableList<Reminder> = mutableListOf(),
    private val onToggle: (Reminder, Boolean) -> Unit,
    private val onDelete: ((Reminder) -> Unit)? = null,
    private val onClick: ((Reminder) -> Unit)? = null
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
        
        // Display title
        holder.tvLabel.text = reminder.title
        
        // Display time from cronExpression or oneTimeAt
        holder.tvTime.text = when {
            reminder.cronExpression != null -> {
                parseCronExpression(reminder.cronExpression)
            }
            reminder.oneTimeAt != null -> {
                parseOneTimeAt(reminder.oneTimeAt)
            }
            else -> "Không rõ thời gian"
        }
        
        // Set switch state without triggering listener
        holder.switchActive.setOnCheckedChangeListener(null)
        holder.switchActive.isChecked = reminder.isActive
        
        // Handle switch toggle
        holder.switchActive.setOnCheckedChangeListener { _, isChecked ->
            onToggle(reminder, isChecked)
        }
        
        // Handle click to select reminder
        holder.itemView.setOnClickListener {
            onClick?.invoke(reminder)
        }
        
        // Handle long click for delete (if callback provided)
        onDelete?.let { deleteCallback ->
            holder.itemView.setOnLongClickListener {
                deleteCallback(reminder)
                true
            }
        }
    }

    override fun getItemCount(): Int = reminders.size

    fun updateReminders(newReminders: List<Reminder>) {
        reminders.clear()
        reminders.addAll(newReminders)
        notifyDataSetChanged()
    }
    
    fun addReminder(reminder: Reminder) {
        reminders.add(reminder)
        notifyItemInserted(reminders.size - 1)
    }
    
    fun removeReminder(reminder: Reminder) {
        val index = reminders.indexOf(reminder)
        if (index != -1) {
            reminders.removeAt(index)
            notifyItemRemoved(index)
        }
    }
    
    fun updateReminder(reminder: Reminder) {
        val index = reminders.indexOfFirst { it.id == reminder.id }
        if (index != -1) {
            reminders[index] = reminder
            notifyItemChanged(index)
        }
    }
    
    private fun parseCronExpression(cron: String): String {
        // Parse cron: "0 8 * * *" -> "08:00"
        val parts = cron.split(" ")
        if (parts.size >= 2) {
            val minute = parts[0].padStart(2, '0')
            val hour = parts[1].padStart(2, '0')
            return "$hour:$minute"
        }
        return cron
    }
    
    private fun parseOneTimeAt(isoDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(isoDate)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: isoDate
        } catch (e: Exception) {
            // Try without milliseconds
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
}
