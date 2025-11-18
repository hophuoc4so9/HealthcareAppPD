package com.example.healthcareapppd.presentation.ui.user.reminder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.healthcareapppd.data.ReminderRepository
import com.example.healthcareapppd.data.local.ReminderDatabase
import com.example.healthcareapppd.data.local.ReminderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReminderRepository
    val allReminders: LiveData<List<ReminderEntity>>

    private val _saveResult = MutableLiveData<Boolean?>()
    val saveResult: LiveData<Boolean?> = _saveResult

    private val _updateResult = MutableLiveData<Boolean?>()
    val updateResult: LiveData<Boolean?> = _updateResult

    private val _deleteResult = MutableLiveData<Boolean?>()
    val deleteResult: LiveData<Boolean?> = _deleteResult

    init {
        val reminderDao = ReminderDatabase.getDatabase(application).reminderDao()
        repository = ReminderRepository(reminderDao)
        allReminders = repository.getAllReminders()
    }

    /** ------------------------ THÊM MỚI ------------------------ **/
    fun insert(reminder: ReminderEntity) {
        _saveResult.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertReminder(reminder)
                _saveResult.postValue(true)
            } catch (e: Exception) {
                e.printStackTrace()
                _saveResult.postValue(false)
            }
        }
    }

    /** ------------------------ CẬP NHẬT ------------------------ **/
    fun update(reminder: ReminderEntity) {
        _updateResult.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateReminder(reminder)
                _updateResult.postValue(true)
            } catch (e: Exception) {
                e.printStackTrace()
                _updateResult.postValue(false)
            }
        }
    }

    /** ------------------------ XÓA ------------------------ **/
    fun delete(reminder: ReminderEntity) {
        _deleteResult.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteReminder(reminder)
                _deleteResult.postValue(true)
            } catch (e: Exception) {
                e.printStackTrace()
                _deleteResult.postValue(false)
            }
        }
    }
}
