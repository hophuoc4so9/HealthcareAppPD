package com.example.healthcareapppd.presentation.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.data.api.model.Conversation
import com.example.healthcareapppd.R
import java.text.SimpleDateFormat
import java.util.*

class ChatSessionAdapter(
    private val conversations: MutableList<Conversation>,
    private val currentUserRole: String,
    private val onClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ChatSessionAdapter.ChatSessionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatSessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_session, parent, false)
        return ChatSessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatSessionViewHolder, position: Int) {
        holder.bind(conversations[position], currentUserRole, onClick)
    }

    override fun getItemCount() = conversations.size

    fun updateConversations(newConversations: List<Conversation>) {
        conversations.clear()
        conversations.addAll(newConversations)
        notifyDataSetChanged()
    }

    class ChatSessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val vOnlineStatus: View = itemView.findViewById(R.id.vOnlineStatus)
        private val tvUnreadCount: TextView = itemView.findViewById(R.id.tvUnreadCount)

        fun bind(conversation: Conversation, currentUserRole: String, onClick: (Conversation) -> Unit) {
            // Hiển thị tên người chat (nếu là patient thì hiển thị tên doctor và ngược lại)
            val displayName = if (currentUserRole == "patient") {
                conversation.doctorName ?: "Bác sĩ"
            } else {
                conversation.patientName ?: "Bệnh nhân"
            }
            tvName.text = displayName
            
            tvLastMessage.text = conversation.lastMessage ?: "Chưa có tin nhắn"
            
            // Hiển thị thời gian
            if (conversation.lastMessageTime != null) {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                    val date = dateFormat.parse(conversation.lastMessageTime)
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    tvTime.text = date?.let { timeFormat.format(it) } ?: ""
                } catch (e: Exception) {
                    tvTime.text = ""
                }
            } else {
                tvTime.text = ""
            }

            // Hiển thị trạng thái và số tin nhắn chưa đọc
            val hasUnread = conversation.unreadCount > 0
            if (hasUnread) {
                tvStatus.text = "Có tin nhắn mới"
                tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                vOnlineStatus.visibility = View.VISIBLE
                tvUnreadCount.visibility = View.VISIBLE
                tvUnreadCount.text = conversation.unreadCount.toString()
            } else {
                tvStatus.text = "Đã đọc"
                tvStatus.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                vOnlineStatus.visibility = View.GONE
                tvUnreadCount.visibility = View.GONE
            }

            itemView.setOnClickListener { onClick(conversation) }
        }
    }
}