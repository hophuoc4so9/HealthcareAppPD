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

        fun bind(session: ChatSession, onClick: (ChatSession) -> Unit) {
            tvName.text = session.name
            tvLastMessage.text = session.lastMessage
            tvStatus.text = if (session.isOngoing) "Đang tiếp diễn" else "Đã đóng"

            itemView.setOnClickListener { onClick(session) }
        }
    }
}