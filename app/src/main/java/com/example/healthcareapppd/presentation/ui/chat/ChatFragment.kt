package com.example.healthcareapppd.presentation.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.api.model.ChatMessage
import com.example.healthcareapppd.domain.usecase.chat.GetMessagesUseCase
import com.example.healthcareapppd.domain.usecase.chat.SendMessageUseCase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var progressBar: ProgressBar
    private val messages = mutableListOf<ChatMessage>()
    private val getMessagesUseCase = GetMessagesUseCase()
    private val sendMessageUseCase = SendMessageUseCase()
    private var conversationId: String? = null
    private var conversationName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        conversationId = arguments?.getString("conversationId")
        conversationName = arguments?.getString("conversationName")

        recyclerView = view.findViewById(R.id.rvChat)
        progressBar = view.findViewById(R.id.progressBar)
        val etMessage = view.findViewById<EditText>(R.id.etMessage)
        val btnSend = view.findViewById<FloatingActionButton>(R.id.btnSend)
        val tvDoctorName = view.findViewById<TextView>(R.id.tvDoctorName)

        // Hiển thị tên người chat
        tvDoctorName.text = conversationName ?: "Chat"

        // Setup RecyclerView
        val currentUserId = com.example.healthcareapppd.utils.TokenManager.getInstance()?.getUserId() ?: ""
        chatAdapter = ChatAdapter(messages, currentUserId)
        recyclerView.adapter = chatAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true // Bắt đầu từ dưới lên
        recyclerView.layoutManager = layoutManager

        // Load tin nhắn từ API
        loadMessages()

        // Gửi tin nhắn
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                etMessage.text.clear()
            }
        }

        return view
    }

    private fun loadMessages() {
        val convId = conversationId
        
        if (convId == null) {
            Toast.makeText(requireContext(), "Không tìm thấy cuộc trò chuyện", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            getMessagesUseCase(requireContext(), convId, limit = 50).fold(
                onSuccess = { messageList ->
                    progressBar.visibility = View.GONE
                    chatAdapter.updateMessages(messageList)
                    if (messageList.isNotEmpty()) {
                        recyclerView.smoothScrollToPosition(messageList.size - 1)
                    }
                },
                onFailure = { error ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Lỗi tải tin nhắn: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun sendMessage(messageContent: String) {
        val convId = conversationId
        
        if (convId == null) {
            Toast.makeText(requireContext(), "Không thể gửi tin nhắn", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            sendMessageUseCase(requireContext(), convId, messageContent).fold(
                onSuccess = { newMessage ->
                    chatAdapter.addMessage(newMessage)
                    recyclerView.smoothScrollToPosition(messages.size - 1)
                },
                onFailure = { error ->
                    Toast.makeText(
                        requireContext(),
                        "Lỗi gửi tin nhắn: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
}