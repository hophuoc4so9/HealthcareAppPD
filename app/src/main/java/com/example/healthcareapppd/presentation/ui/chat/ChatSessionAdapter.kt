package com.example.healthcareapppd.presentation.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.domain.usecase.ChatSession
import com.example.healthcareapppd.R

class ChatSessionAdapter(
    private val sessions: List<ChatSession>,
    private val onClick: (ChatSession) -> Unit
) : RecyclerView.Adapter<ChatSessionAdapter.ChatSessionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatSessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_session, parent, false)
        return ChatSessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatSessionViewHolder, position: Int) {
        holder.bind(sessions[position], onClick)
    }

    override fun getItemCount() = sessions.size

    class ChatSessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val vOnlineStatus: View = itemView.findViewById(R.id.vOnlineStatus)
        private val tvUnreadCount: TextView = itemView.findViewById(R.id.tvUnreadCount)

        fun bind(session: ChatSession, onClick: (ChatSession) -> Unit) {
            tvName.text = session.name
            tvLastMessage.text = session.lastMessage
            tvTime.text = "10:30" // Có thể thêm thời gian thật từ data

            // Hiển thị trạng thái
            if (session.isOngoing) {
                tvStatus.text = "Đang hoạt động"
                tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                vOnlineStatus.visibility = View.VISIBLE

                // Hiển thị badge tin nhắn chưa đọc (demo)
                tvUnreadCount.visibility = View.VISIBLE
                tvUnreadCount.text = "2"
            } else {
                tvStatus.text = "Đã đóng"
                tvStatus.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                vOnlineStatus.visibility = View.GONE
                tvUnreadCount.visibility = View.GONE
            }

            itemView.setOnClickListener { onClick(session) }
        }
    }
}