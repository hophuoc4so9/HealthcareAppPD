package com.example.healthcareapppd.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ReminderEntity::class], version = 2, exportSchema = false)
abstract class ReminderDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao

    companion object {

        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Kiểm tra và chỉ thêm cột nếu chưa tồn tại
                val cursor = database.query("PRAGMA table_info(reminders)")
                val existingColumns = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    existingColumns.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
                }
                cursor.close()

                if (!existingColumns.contains("label")) {
                    database.execSQL("ALTER TABLE reminders ADD COLUMN label TEXT DEFAULT ''")
                }
                if (!existingColumns.contains("date")) {
                    database.execSQL("ALTER TABLE reminders ADD COLUMN date TEXT")
                }
                if (!existingColumns.contains("repeat")) {
                    database.execSQL("ALTER TABLE reminders ADD COLUMN repeat TEXT")
                }
                if (!existingColumns.contains("title")) {
                    database.execSQL("ALTER TABLE reminders ADD COLUMN title TEXT")
                }
            }
        }

        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        fun getDatabase(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "reminder_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
