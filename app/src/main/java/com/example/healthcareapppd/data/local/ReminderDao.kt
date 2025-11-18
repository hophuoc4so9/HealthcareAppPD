package com.example.healthcareapppd.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity)

    // Lấy danh sách tất cả nhắc hẹn, sắp xếp theo thời gian tăng dần
    @Query("SELECT * FROM reminders ORDER BY time ASC")
    fun getAllRemindersLiveData(): LiveData<List<ReminderEntity>>

    // Truy vấn không dùng LiveData (nếu cần xử lý trong coroutine)
    @Query("SELECT * FROM reminders ORDER BY time ASC")
    suspend fun getAll(): List<ReminderEntity>

    // Cập nhật nhắc hẹn
    @Update
    suspend fun update(reminder: ReminderEntity)

    // Xóa nhắc hẹn cụ thể
    @Delete
    suspend fun delete(reminder: ReminderEntity)

    // ✅ Xóa tất cả nhắc hẹn (tuỳ chọn – tiện khi cần reset dữ liệu)
    @Query("DELETE FROM reminders")
    suspend fun deleteAll()

    // ✅ Lấy nhắc hẹn theo id (tiện cho chức năng chỉnh sửa)
    @Query("SELECT * FROM reminders WHERE id = :reminderId LIMIT 1")
    suspend fun getById(reminderId: Int): ReminderEntity?
}
