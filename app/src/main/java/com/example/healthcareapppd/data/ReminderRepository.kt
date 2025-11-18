package com.example.healthcareapppd.data

import androidx.lifecycle.LiveData
import com.example.healthcareapppd.data.local.ReminderDao
import com.example.healthcareapppd.data.local.ReminderEntity

class ReminderRepository(private val reminderDao: ReminderDao) {
    fun getAllReminders(): LiveData<List<ReminderEntity>> {
        return reminderDao.getAllRemindersLiveData()
    }

    suspend fun insertReminder(reminder: ReminderEntity) = reminderDao.insert(reminder)

    // Giữ hàm này để gọi hàm suspend getAll() nếu cần thiết
    suspend fun getAllRemindersOnce() = reminderDao.getAll()
    suspend fun updateReminder(reminder: ReminderEntity) = reminderDao.update(reminder)
    suspend fun deleteReminder(reminder: ReminderEntity) = reminderDao.delete(reminder)
}
