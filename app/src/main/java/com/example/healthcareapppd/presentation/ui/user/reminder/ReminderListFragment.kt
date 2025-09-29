package com.example.healthcareapppd.presentation.ui.user.reminder

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapppd.R
import com.example.healthcareapppd.presentation.ui.reminder.Reminder
import com.example.healthcareapppd.presentation.ui.reminder.ReminderAdapter

class ReminderListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReminderAdapter
    private val reminders = mutableListOf<Reminder>()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.reminder_fragment_item_list, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_reminders)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReminderAdapter(reminders)
        recyclerView.adapter = adapter

        // Nhận dữ liệu từ AddReminderFragment nếu có
        parentFragmentManager.setFragmentResultListener("addReminderKey", viewLifecycleOwner) { _, bundle ->
            val time = bundle.getString("time") ?: "00:00"
            val label = bundle.getString("label") ?: "Không có nhãn"
            val isActive = bundle.getBoolean("isActive", true)

            adapter.addReminder(Reminder(time, label, isActive))
        }

        return view
    }
}
