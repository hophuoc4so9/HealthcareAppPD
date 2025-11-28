package com.example.healthcareapppd.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import kotlinx.coroutines.*

class DoctorChatFragment : Fragment() {
    
    private lateinit var recyclerViewConversations: RecyclerView
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var conversationContainer: LinearLayout
    private lateinit var chatContainer: LinearLayout
    
    private lateinit var conversationsAdapter: ConversationsAdapter
    private lateinit var messagesAdapter: MessagesAdapter
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var selectedConversationId: String? = null
    private var currentUserId: String? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_doctor_chat, container, false)
        
        recyclerViewConversations = view.findViewById(R.id.recyclerViewConversations)
        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages)
        etMessage = view.findViewById(R.id.etMessage)
        btnSend = view.findViewById(R.id.btnSend)
        conversationContainer = view.findViewById(R.id.conversationContainer)
        chatContainer = view.findViewById(R.id.chatContainer)
        
        currentUserId = getCurrentUserId()
        
        setupConversations()
        setupMessages()
        setupSendButton()
        
        loadConversations()
        
        return view
    }
    
    private fun setupConversations() {
        conversationsAdapter = ConversationsAdapter { conversation ->
            selectedConversationId = conversation.id
            showChatWindow(conversation)
            loadMessages(conversation.id)
        }
        recyclerViewConversations.layoutManager = LinearLayoutManager(context)
        recyclerViewConversations.adapter = conversationsAdapter
    }
    
    private fun setupMessages() {
        messagesAdapter = MessagesAdapter(currentUserId ?: "")
        recyclerViewMessages.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        recyclerViewMessages.adapter = messagesAdapter
    }
    
    private fun setupSendButton() {
        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty() && selectedConversationId != null) {
                sendMessage(message)
            }
        }
    }
    
    private fun showChatWindow(conversation: Conversation) {
        conversationContainer.visibility = View.GONE
        chatContainer.visibility = View.VISIBLE
    }
    
    private fun loadConversations() {
        scope.launch {
            try {
                val token = getAuthToken()
                android.util.Log.d("DoctorChatFragment", "Token used for API: $token")
                val response = withContext(Dispatchers.IO) {
                    ApiService.getMyConversations(token)
                }
                
                if (response.success) {
                    val conversations = response.data.conversations ?: emptyList()
                    conversationsAdapter.submitList(conversations)
                } else {
                    Toast.makeText(context, "Lỗi: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Không thể tải danh sách hội thoại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadMessages(conversationId: String) {
        scope.launch {
            try {
                val token = getAuthToken()
                android.util.Log.d("DoctorChatFragment", "Token used for API: $token")
                val response = withContext(Dispatchers.IO) {
                    ApiService.getConversationMessages(token, conversationId)
                }
                
                if (response.success) {
                    val messages = response.data.messages ?: emptyList()
                    messagesAdapter.submitList(messages)
                    if (messages.isNotEmpty()) {
                        recyclerViewMessages.scrollToPosition(messages.size - 1)
                    }
                } else {
                    Toast.makeText(context, "Lỗi: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Không thể tải tin nhắn: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun sendMessage(message: String) {
        scope.launch {
            try {
                val token = getAuthToken()
                android.util.Log.d("DoctorChatFragment", "Token used for API: $token")
                val conversationId = selectedConversationId ?: return@launch

                val response = withContext(Dispatchers.IO) {
                    ApiService.sendMessage(token, conversationId, message)
                }
                
                if (response.success) {
                    etMessage.setText("")
                    loadMessages(conversationId)
                    Toast.makeText(context, "Đã gửi tin nhắn", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Lỗi: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Không thể gửi tin nhắn: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun getAuthToken(): String {
        val tokenFromManager = com.example.healthcareapppd.utils.TokenManager.getToken(requireContext())
        if (!tokenFromManager.isNullOrEmpty()) {
            android.util.Log.d("DoctorChatFragment", "TokenManager token: $tokenFromManager")
            return tokenFromManager
        }
        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val token = sharedPrefs.getString("doctor_token", "") ?: ""
        android.util.Log.d("DoctorChatFragment", "SharedPrefs token: $token")
        if (token.isEmpty()) {
            android.util.Log.e("DoctorChatFragment", "Token is empty! API will fail.")
        }
        return token
    }
    
    private fun getCurrentUserId(): String {
        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        return sharedPrefs.getString("user_id", "") ?: ""
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

// Conversations Adapter
class ConversationsAdapter(
    private val onItemClick: (Conversation) -> Unit
) : androidx.recyclerview.widget.ListAdapter<Conversation, ConversationsAdapter.ViewHolder>(
    ConversationDiffCallback()
) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPatientName: TextView = itemView.findViewById(R.id.tvPatientName)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val badgeUnread: TextView = itemView.findViewById(R.id.badgeUnread)
        
        fun bind(conversation: Conversation, onClick: (Conversation) -> Unit) {
            tvPatientName.text = conversation.patientName
            tvLastMessage.text = conversation.lastMessage ?: "Chưa có tin nhắn"
            tvTime.text = conversation.lastMessageTime ?: ""
            
            if (conversation.unreadCount > 0) {
                badgeUnread.visibility = View.VISIBLE
                badgeUnread.text = conversation.unreadCount.toString()
            } else {
                badgeUnread.visibility = View.GONE
            }
            
            itemView.setOnClickListener { onClick(conversation) }
        }
    }
}

// Messages Adapter
class MessagesAdapter(
    private val currentUserId: String
) : androidx.recyclerview.widget.ListAdapter<Message, MessagesAdapter.ViewHolder>(
    MessageDiffCallback()
) {
    
    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderUserId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutRes = if (viewType == VIEW_TYPE_SENT) {
            R.layout.item_message_sent
        } else {
            R.layout.item_message_received
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        
        fun bind(message: Message) {
            tvMessage.text = message.messageContent
            tvTime.text = message.createdAt
        }
    }
    
    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }
}

class ConversationDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<Conversation>() {
    override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation) = oldItem == newItem
}

class MessageDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem
}