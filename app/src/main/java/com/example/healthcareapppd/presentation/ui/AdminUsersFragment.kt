package com.example.healthcareapppd.presentation.ui

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
// Import Model và ApiService cùng package
// import com.example.healthcareapppd.presentation.ui.ApiService
// import com.example.healthcareapppd.presentation.ui.User
import com.example.healthcareapppd.utils.TokenManager
import kotlinx.coroutines.*

class AdminUsersFragment : Fragment() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminUsersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_list, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewAdmin)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = AdminUsersAdapter { user ->
            showActionDialog(user)
        }
        recyclerView.adapter = adapter

        loadUsers()
        return view
    }

    private fun loadUsers() {
        scope.launch {
            try {
                val token = TokenManager.getToken(requireContext()) ?: return@launch
                val response = withContext(Dispatchers.IO) {
                    ApiService.getAllUsers(token)
                }
                if (response.success) {
                    adapter.submitList(response.data.users)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun showActionDialog(user: User) {
        // Logic hiển thị text: Nếu đang Banned -> Hiển thị "Mở khóa", ngược lại "Khóa"
        val banActionText = if (user.is_banned) "Mở khóa (Unban)" else "Khóa tài khoản (Ban)"

        val options = arrayOf("Đổi quyền (Change Role)", banActionText)

        AlertDialog.Builder(context)
            .setTitle("Thao tác: ${user.email}")
            .setItems(options) { _, which ->
                when(which) {
                    0 -> showRoleDialog(user)
                    1 -> banUnbanUser(user)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showRoleDialog(user: User) {
        val roles = arrayOf("patient", "doctor", "admin")
        AlertDialog.Builder(context)
            .setTitle("Chọn quyền mới")
            .setItems(roles) { _, which ->
                updateRole(user.id, roles[which])
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateRole(userId: String, newRole: String) {
        scope.launch {
            val token = TokenManager.getToken(requireContext()) ?: return@launch
            val response = withContext(Dispatchers.IO) {
                ApiService.updateUserRole(token, userId, newRole)
            }
            if(response.success) {
                Toast.makeText(context, "Đã cập nhật role", Toast.LENGTH_SHORT).show()
                loadUsers()
            } else {
                Toast.makeText(context, "Lỗi: ${response.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun banUnbanUser(user: User) {
        scope.launch {
            val token = TokenManager.getToken(requireContext()) ?: return@launch
            val response = withContext(Dispatchers.IO) {
                // Truyền trạng thái hiện tại (is_banned) vào API để nó đảo ngược
                ApiService.banUnbanUser(token, user.id, user.is_banned)
            }
            if(response.success) {
                val msg = if (user.is_banned) "Đã mở khóa" else "Đã khóa tài khoản"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                loadUsers()
            } else {
                Toast.makeText(context, "Lỗi: ${response.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

// --- ADAPTER CẬP NHẬT ---
class AdminUsersAdapter(private val onClick: (User) -> Unit) : RecyclerView.Adapter<AdminUsersAdapter.ViewHolder>() {
    private var users = listOf<User>()

    fun submitList(list: List<User>) {
        users = list
        notifyDataSetChanged()
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // Các ID này bây giờ sẽ được tìm thấy
        val tvEmail: TextView = v.findViewById(R.id.tvUserEmail)
        val tvRole: TextView = v.findViewById(R.id.tvUserRole)
        val tvStatus: TextView = v.findViewById(R.id.tvUserStatus)
        val tvDate: TextView = v.findViewById(R.id.tvUserDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Đảm bảo tên file layout đúng là item_admin_user
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_user, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        holder.tvEmail.text = user.email
        holder.tvRole.text = user.role.uppercase()

        // Format ngày (Lấy 10 ký tự đầu: YYYY-MM-DD)
        val dateStr = if (user.created_at.length >= 10) user.created_at.substring(0, 10) else user.created_at
        holder.tvDate.text = "Ngày tạo: $dateStr"

        // Xử lý màu sắc Role
        when (user.role) {
            "admin" -> {
                holder.tvRole.setTextColor(Color.parseColor("#D32F2F")) // Red
                holder.tvRole.setBackgroundColor(Color.parseColor("#FFEBEE"))
            }
            "doctor" -> {
                holder.tvRole.setTextColor(Color.parseColor("#1976D2")) // Blue
                holder.tvRole.setBackgroundColor(Color.parseColor("#E3F2FD"))
            }
            else -> { // patient
                holder.tvRole.setTextColor(Color.parseColor("#388E3C")) // Green
                holder.tvRole.setBackgroundColor(Color.parseColor("#E8F5E9"))
            }
        }

        // Xử lý trạng thái Ban/Active
        if (user.is_banned) {
            holder.tvStatus.text = "BANNED"
            holder.tvStatus.setTextColor(Color.RED)
            // Có thể làm mờ email nếu muốn
            holder.tvEmail.alpha = 0.5f
        } else if (user.is_active) {
            holder.tvStatus.text = "Active"
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
            holder.tvEmail.alpha = 1.0f
        } else {
            holder.tvStatus.text = "Inactive"
            holder.tvStatus.setTextColor(Color.GRAY)
            holder.tvEmail.alpha = 1.0f
        }

        holder.itemView.setOnClickListener { onClick(user) }
    }

    override fun getItemCount() = users.size
}