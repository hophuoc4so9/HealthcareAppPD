package com.example.healthcareapppd.presentation.ui.chat

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.domain.usecase.ChatMessage
import com.example.healthcareapppd.R
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val container: LinearLayout = itemView.findViewById(R.id.containerMessage)

        fun bind(message: ChatMessage) {
            tvMessage.text = message.message

            // Hiển thị thời gian
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = timeFormat.format(Date())

            // Nếu là mình gửi → căn phải, nền xanh
            if (message.isSender) {
                container.gravity = Gravity.END
                tvMessage.setBackgroundResource(R.drawable.bg_chat_sender)
                tvMessage.setTextColor(Color.WHITE)
                tvTime.gravity = Gravity.END
            } else {
                // Nếu là người nhận → căn trái, nền trắng
                container.gravity = Gravity.START
                tvMessage.setBackgroundResource(R.drawable.bg_chat_receiver)
                tvMessage.setTextColor(Color.parseColor("#212121"))
                tvTime.gravity = Gravity.START
            }
        }
    }
}