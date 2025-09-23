package com.example.healthcareapppd.presentation.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.domain.usecase.ChatMessage

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recyclerView = view.findViewById(R.id.rvChat)
        val etMessage = view.findViewById<EditText>(R.id.etMessage)
        val btnSend = view.findViewById<Button>(R.id.btnSend)

        // Setup RecyclerView
        chatAdapter = ChatAdapter(messages)
        recyclerView.adapter = chatAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Tin nhắn mẫu ban đầu
        messages.add(ChatMessage("Xin chào bác sĩ 👋", true))  // user gửi
        messages.add(ChatMessage("Chào bạn, tôi có thể giúp gì?", false)) // bác sĩ trả lời
        chatAdapter.notifyDataSetChanged()

        // Gửi tin nhắn
        btnSend.setOnClickListener {
            val text = etMessage.text.toString()
            if (text.isNotEmpty()) {
                // user gửi
                messages.add(ChatMessage(text, true))
                chatAdapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)
                etMessage.text.clear()

                // Giả lập bác sĩ trả lời sau 1s
                recyclerView.postDelayed({
                    messages.add(ChatMessage("Bác sĩ trả lời: $text", false))
                    chatAdapter.notifyItemInserted(messages.size - 1)
                    recyclerView.scrollToPosition(messages.size - 1)
                }, 1000)
            }
        }

        return view
    }
}
