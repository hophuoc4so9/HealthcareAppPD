package com.example.healthcareapppd.presentation.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.api.model.Conversation
import com.example.healthcareapppd.domain.usecase.chat.GetConversationsUseCase
import kotlinx.coroutines.launch


class MessageListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatSessionAdapter
    private lateinit var progressBar: ProgressBar
    private val conversations = mutableListOf<Conversation>()
    private val getConversationsUseCase = GetConversationsUseCase()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message_list, container, false)
        
        recyclerView = view.findViewById(R.id.rvSessions)
        progressBar = view.findViewById(R.id.progressBar)
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val userRole = com.example.healthcareapppd.utils.TokenManager.getInstance()?.getUserRole() ?: "patient"
        adapter = ChatSessionAdapter(conversations, userRole) { conversation ->
            // Mở ChatFragment với conversationId
            val bundle = bundleOf(
                "conversationId" to conversation.id,
                "conversationName" to if (userRole == "patient") conversation.doctorName else conversation.patientName
            )
            findNavController().navigate(R.id.action_messageList_to_chat, bundle)
        }
        recyclerView.adapter = adapter

        loadConversations()

        return view
    }

    private fun loadConversations() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            getConversationsUseCase(requireContext()).fold(
                onSuccess = { conversationList ->
                    progressBar.visibility = View.GONE
                    adapter.updateConversations(conversationList)
                    
                    if (conversationList.isEmpty()) {
                        Toast.makeText(requireContext(), "Chưa có cuộc trò chuyện nào", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { error ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Lỗi tải danh sách: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
}