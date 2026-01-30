package com.routinetool.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.routinetool.data.local.entities.SubtaskEntity
import com.routinetool.data.local.entities.TaskEntity

@Database(
    entities = [TaskEntity::class, SubtaskEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun subtaskDao(): SubtaskDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS subtasks (
                id TEXT NOT NULL PRIMARY KEY,
                taskId TEXT NOT NULL,
                title TEXT NOT NULL,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                completedAt INTEGER,
                position REAL NOT NULL,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY (taskId) REFERENCES tasks(id) ON DELETE CASCADE
            )
        """.trimIndent())

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_subtasks_taskId ON subtasks(taskId)
        """.trimIndent())
    }
}
