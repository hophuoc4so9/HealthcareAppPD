package com.example.healthcareapppd.presentation.ui.chat

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.data.api.model.ChatMessage
import com.example.healthcareapppd.R
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val messages: MutableList<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position], currentUserId)
    }

    override fun getItemCount() = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val container: LinearLayout = itemView.findViewById(R.id.containerMessage)

        fun bind(message: ChatMessage, currentUserId: String) {
            tvMessage.text = message.messageContent

            // Hiển thị thời gian
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = dateFormat.parse(message.sentAt)
                tvTime.text = date?.let { timeFormat.format(it) } ?: ""
            } catch (e: Exception) {
                tvTime.text = ""
            }

            // Nếu là mình gửi → căn phải, nền xanh
            val isSender = message.senderUserId == currentUserId
            if (isSender) {
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