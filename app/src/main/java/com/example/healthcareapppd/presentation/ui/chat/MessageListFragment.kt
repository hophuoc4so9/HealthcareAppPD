package com.example.healthcareapppd.presentation.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.domain.usecase.ChatSession


class MessageListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatSessionAdapter
    private val sessions = mutableListOf<ChatSession>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message_list, container, false)
        recyclerView = view.findViewById(R.id.rvSessions)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Dữ liệu demo
        sessions.add(ChatSession("1", "Bác sĩ A", "Hẹn gặp lại bạn nhé!", false))
        sessions.add(ChatSession("2", "Bác sĩ B", "Bạn nhớ uống thuốc đúng giờ", true))
        sessions.add(ChatSession("3", "Bác sĩ C", "Bạn cần tái khám sau 2 tuần", false))
        sessions.add(ChatSession("4", "Bác sĩ D", "Chào bạn, bạn cần hỗ trợ gì?", true))

        adapter = ChatSessionAdapter(sessions) { session ->
            if (session.isOngoing) {
                // Mở ChatFragment để tiếp tục trò chuyện
                findNavController().navigate(R.id.action_messageList_to_chat)

            } else {
                Toast.makeText(requireContext(), "Cuộc trò chuyện đã đóng", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = adapter

        return view
    }
}