package com.example.healthcareapppd.presentation.ui.reminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.local.ReminderEntity
import com.google.android.material.switchmaterial.SwitchMaterial


class ReminderAdapter(
    // Thêm callback để Fragment/ViewModel xử lý việc cập nhật DB
    private val onSwitchToggle: (ReminderEntity, Boolean) -> Unit,
    private val onItemClick: (ReminderEntity) -> Unit
) : ListAdapter<ReminderEntity, ReminderAdapter.ReminderViewHolder>(ReminderDiffCallback()) {
// Adapter nhận vào ReminderEntity

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
        val reminder = getItem(position)
        holder.tvTime.text = reminder.time
        holder.tvLabel.text = reminder.label

        holder.switchActive.setOnCheckedChangeListener(null)
        holder.switchActive.isChecked = reminder.isActive

        // Sử dụng Callback để thông báo cho ViewModel
        holder.switchActive.setOnCheckedChangeListener { _, isChecked ->
            onSwitchToggle(reminder, isChecked)
        }

        holder.itemView.setOnClickListener {
            onItemClick(reminder)
        }
    }
}

class ReminderDiffCallback : DiffUtil.ItemCallback<ReminderEntity>() {
    override fun areItemsTheSame(oldItem: ReminderEntity, newItem: ReminderEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ReminderEntity, newItem: ReminderEntity): Boolean {
        return oldItem == newItem
    }
}