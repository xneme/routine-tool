package com.routinetool.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.routinetool.data.local.entities.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
