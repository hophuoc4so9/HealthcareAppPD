package com.example.healthcareapppd.presentation.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.domain.usecase.ChatMessage
import com.google.android.material.floatingactionbutton.FloatingActionButton

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
        val btnSend = view.findViewById<FloatingActionButton>(R.id.btnSend)
        val tvDoctorName = view.findViewById<TextView>(R.id.tvDoctorName)

        // Hiển thị tên bác sĩ
        tvDoctorName.text = "Bác sĩ Nguyễn Văn A"

        // Setup RecyclerView
        chatAdapter = ChatAdapter(messages)
        recyclerView.adapter = chatAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true // Bắt đầu từ dưới lên
        recyclerView.layoutManager = layoutManager

        // Tin nhắn mẫu ban đầu
        messages.add(ChatMessage("Xin chào bác sĩ 👋", true))
        messages.add(ChatMessage("Chào bạn! Tôi có thể giúp gì cho bạn hôm nay?", false))
        chatAdapter.notifyDataSetChanged()

        // Gửi tin nhắn
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                // User gửi
                messages.add(ChatMessage(text, true))
                chatAdapter.notifyItemInserted(messages.size - 1)
                recyclerView.smoothScrollToPosition(messages.size - 1)
                etMessage.text.clear()

                // Giả lập bác sĩ trả lời sau 1.5s
                recyclerView.postDelayed({
                    val responses = listOf(
                        "Cảm ơn bạn đã chia sẻ!",
                        "Tôi hiểu rồi. Bạn có thắc mắc gì thêm không?",
                        "Để tôi kiểm tra thông tin này nhé.",
                        "Bạn nên uống thuốc đúng giờ và theo dõi tình trạng sức khỏe."
                    )
                    messages.add(ChatMessage(responses.random(), false))
                    chatAdapter.notifyItemInserted(messages.size - 1)
                    recyclerView.smoothScrollToPosition(messages.size - 1)
                }, 1500)
            }
        }

        return view
    }
}