package com.example.healthcareapppd.data.local

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val time: String,
    val label: String,
    val isActive: Boolean = true,
    val date: String? = null,
    val repeat: String? = null,
    val title: String? = null
) : Parcelable

