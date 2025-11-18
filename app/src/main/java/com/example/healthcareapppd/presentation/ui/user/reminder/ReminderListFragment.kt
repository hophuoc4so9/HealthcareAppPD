package com.example.healthcareapppd.presentation.ui.user.reminder

import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.data.local.ReminderEntity
import com.example.healthcareapppd.presentation.ui.reminder.ReminderAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ReminderListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReminderAdapter
    private lateinit var fabAdd: FloatingActionButton

    private val viewModel: ReminderViewModel by lazy {
        ViewModelProvider(this).get(ReminderViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.reminder_fragment_item_list, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_reminders)
        fabAdd = view.findViewById(R.id.fab_add)

        setupRecyclerView()

        observeReminders()

        // Xử lý sự kiện nhấn nút Add (UI Event)
        fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_reminderAddFragment)
        }

        return view
    }

    private fun setupRecyclerView() {
        val onSwitchToggle: (ReminderEntity, Boolean) -> Unit = { reminder, isChecked ->
            val updatedReminder = reminder.copy(isActive = isChecked)
            viewModel.update(updatedReminder)
            Toast.makeText(requireContext(), "Đã cập nhật trạng thái nhắc hẹn", Toast.LENGTH_SHORT).show()
        }

        val onItemClick: (ReminderEntity) -> Unit = { reminder ->
            // ✅ Tạo Bundle để truyền đối tượng Reminder sang EditReminderFragment
            val bundle = Bundle().apply {
                putParcelable("reminder", reminder)
            }
            findNavController().navigate(R.id.action_reminderEditFragment, bundle)
        }

        adapter = ReminderAdapter(onSwitchToggle, onItemClick)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }


    private fun observeReminders() {
        viewModel.allReminders.observe(viewLifecycleOwner) { remindersList ->
            // Dữ liệu mới đã có, gửi nó cho ListAdapter
            adapter.submitList(remindersList)
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val reminder = adapter.currentList[position]

                // Hộp thoại xác nhận xóa
                AlertDialog.Builder(requireContext())
                    .setTitle("Xóa nhắc hẹn")
                    .setMessage("Bạn có chắc muốn xóa nhắc hẹn này không?")
                    .setPositiveButton("Xóa") { _, _ ->
                        viewModel.delete(reminder)
                        Toast.makeText(requireContext(), "Đã xóa nhắc hẹn", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Hủy") { dialog, _ ->
                        dialog.dismiss()
                        adapter.notifyItemChanged(position) // khôi phục item
                    }
                    .show()
            }

            // Vẽ nền đỏ + icon thùng rác khi vuốt
            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val paint = Paint()
                paint.color = Color.RED

                if (dX < 0) { // Vuốt sang trái
                    c.drawRect(
                        itemView.right.toFloat() + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat(),
                        paint
                    )

                    val icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_delete_24)
                    icon?.let {
                        val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                        val iconTop = itemView.top + iconMargin
                        val iconLeft = itemView.right - iconMargin - it.intrinsicWidth
                        val iconRight = itemView.right - iconMargin
                        val iconBottom = iconTop + it.intrinsicHeight
                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        it.draw(c)
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }
}