package com.example.healthcareapppd.presentation.ui.chat

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import com.example.healthcareapppd.utils.ChatConstants
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatFragment : Fragment() {

    // ... (Khai báo view như cũ) ...
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var etMessage: EditText // Đưa lên đây để dùng chung
    private val messages = mutableListOf<ChatMessage>()

    // UseCase Server
    private val getMessagesUseCase = GetMessagesUseCase()
    private val sendMessageUseCase = SendMessageUseCase()

    // Biến cho AI
    private var conversationId: String? = null
    private var conversationName: String? = null
    private var isPolling = false
    private var isAiMode = false

    // Khởi tạo Gemini Model (Nhớ thay API Key hoặc dùng BuildConfig)
    private val geminiModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = "AIzaSyCSMz-daEk0gRyZ3mnpglmLao1IJtKRLss"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        conversationId = arguments?.getString("conversationId")
        conversationName = arguments?.getString("conversationName")

        // KIỂM TRA CHẾ ĐỘ AI
        isAiMode = (conversationId == ChatConstants.AI_BOT_ID)

        recyclerView = view.findViewById(R.id.rvChat)
        progressBar = view.findViewById(R.id.progressBar)
        etMessage = view.findViewById(R.id.etMessage)
        val btnSend = view.findViewById<FloatingActionButton>(R.id.btnSend)
        val tvDoctorName = view.findViewById<TextView>(R.id.tvDoctorName)

        tvDoctorName.text = conversationName ?: "Chat"

        val currentUserId = com.example.healthcareapppd.utils.TokenManager.getInstance()?.getUserId() ?: ""
        chatAdapter = ChatAdapter(messages, currentUserId)
        recyclerView.adapter = chatAdapter

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager

        // PHÂN LUỒNG LOGIC TẢI TIN NHẮN
        if (isAiMode) {
            loadAiMessagesLocal() // Tải từ bộ nhớ máy
        } else {
            loadMessagesInitial() // Tải từ Server
            startMessagePolling() // Bắt đầu vòng lặp
        }

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                if (isAiMode) {
                    sendAiMessage(text, currentUserId)
                } else {
                    sendMessage(text)
                }
                etMessage.text.clear()
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isPolling = false
    }

    // --- LOGIC CHO NGƯỜI THẬT (GIỮ NGUYÊN) ---
    private fun loadMessagesInitial() { /* Giữ nguyên code cũ */
        val convId = conversationId ?: return
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            getMessagesUseCase(requireContext(), convId, limit = 50).fold(
                onSuccess = { messageList ->
                    progressBar.visibility = View.GONE
                    chatAdapter.updateMessages(messageList)
                    if (messageList.isNotEmpty()) recyclerView.scrollToPosition(messageList.size - 1)
                },
                onFailure = { progressBar.visibility = View.GONE }
            )
        }
    }

    private fun startMessagePolling() { /* Giữ nguyên code cũ */
        if (isAiMode) return // Nếu là AI thì không chạy polling server
        val convId = conversationId ?: return
        isPolling = true
        lifecycleScope.launch {
            while (isActive && isPolling) {
                try {
                    delay(3000)
                    getMessagesUseCase(requireContext(), convId, limit = 50).fold(
                        onSuccess = { messageList ->
                            if (messageList.size > chatAdapter.itemCount) {
                                chatAdapter.updateMessages(messageList)
                                recyclerView.smoothScrollToPosition(messageList.size - 1)
                            }
                        },
                        onFailure = { Log.e("ChatFragment", "Polling error: ${it.message}") }
                    )
                } catch (e: Exception) { Log.e("ChatFragment", "Lỗi vòng lặp: ${e.message}") }
            }
        }
    }

    private fun sendMessage(messageContent: String) { /* Giữ nguyên code cũ */
        val convId = conversationId ?: return
        lifecycleScope.launch {
            sendMessageUseCase(requireContext(), convId, messageContent).fold(
                onSuccess = { newMessage ->
                    chatAdapter.addMessage(newMessage)
                    recyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                },
                onFailure = { error -> Toast.makeText(context, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show() }
            )
        }
    }

    // --- LOGIC MỚI: AI CHATBOT & LƯU LOCAL ---

    private fun sendAiMessage(content: String, currentUserId: String) {
        // 1. Tạo tin nhắn của User (Hiển thị ngay)
        val userMsg = ChatMessage(
            // SỬA LỖI 1: id phải là Long (bỏ .toString())
            id = System.currentTimeMillis(),
            conversationId = ChatConstants.AI_BOT_ID,
            senderUserId = currentUserId,
            messageContent = content,
            sentAt = getCurrentTimeISO(),
            // SỬA LỖI 2: Thêm các tham số còn thiếu (giá trị giả)
            readAt = null,
            senderEmail = "",
            senderRole = "patient"
        )

        chatAdapter.addMessage(userMsg)
        recyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
        saveMessageToLocal(userMsg)

        // 2. Gọi Gemini API
        lifecycleScope.launch {
            try {
                val prompt = "Bạn là trợ lý y tế Dr. AI. Hãy tư vấn ngắn gọn về: $content. Nếu nguy hiểm hãy khuyên đi khám."
                val response = geminiModel.generateContent(prompt)
                val aiText = response.text ?: "Xin lỗi, tôi không hiểu ý bạn."

                // 3. Tạo tin nhắn của AI Bot
                val aiMsg = ChatMessage(
                    // SỬA LỖI 1: id là Long (cộng thêm 1 để khác id trên)
                    id = System.currentTimeMillis() + 1,
                    conversationId = ChatConstants.AI_BOT_ID,
                    senderUserId = "ai_bot",
                    messageContent = aiText.replace("**", "").replace("* ", "- "),
                    sentAt = getCurrentTimeISO(),
                    // SỬA LỖI 2: Thêm tham số
                    readAt = getCurrentTimeISO(),
                    senderEmail = "dr.ai@healthcare.com",
                    senderRole = "doctor"
                )

                chatAdapter.addMessage(aiMsg)
                recyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                saveMessageToLocal(aiMsg)

            } catch (e: Exception) {
                // Tạo tin nhắn báo lỗi
                val errorMsg = ChatMessage(
                    id = System.currentTimeMillis(),
                    conversationId = ChatConstants.AI_BOT_ID,
                    senderUserId = "ai_bot",
                    messageContent = "Lỗi kết nối AI: ${e.message}",
                    sentAt = getCurrentTimeISO(),
                    // SỬA LỖI 2: Thêm tham số
                    readAt = null,
                    senderEmail = "system",
                    senderRole = "system"
                )
                chatAdapter.addMessage(errorMsg)
            }
        }

    }

    // Hàm lấy thời gian hiện tại chuẩn ISO 8601 để khớp với format của ChatAdapter
    private fun getCurrentTimeISO(): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        df.timeZone = TimeZone.getTimeZone("UTC")
        return df.format(Date())
    }

    // --- LƯU TRỮ LOCAL (Dùng SharedPreferences cho đơn giản) ---
    private fun saveMessageToLocal(message: ChatMessage) {
        val sharedPref = requireContext().getSharedPreferences("AiChatHistory", Context.MODE_PRIVATE)
        val gson = Gson()

        // Lấy danh sách cũ
        val json = sharedPref.getString("messages", null)
        val type = object : TypeToken<MutableList<ChatMessage>>() {}.type
        val localList: MutableList<ChatMessage> = if (json != null) {
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }

        // Thêm tin mới
        localList.add(message)

        // Lưu lại
        val editor = sharedPref.edit()
        editor.putString("messages", gson.toJson(localList))
        editor.apply()
    }

    private fun loadAiMessagesLocal() {
        val sharedPref = requireContext().getSharedPreferences("AiChatHistory", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPref.getString("messages", null)

        if (json != null) {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            val localList: List<ChatMessage> = gson.fromJson(json, type)
            chatAdapter.updateMessages(localList)
            if (localList.isNotEmpty()) {
                recyclerView.scrollToPosition(localList.size - 1)
            }
        }
    }
}