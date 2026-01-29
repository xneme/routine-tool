package com.routinetool.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.routinetool.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // Active tasks ordered by deadline proximity (overdue first, then by nearest deadline, no-deadline last)
    @Query("""
        SELECT * FROM tasks
        WHERE isCompleted = 0
        ORDER BY
            CASE
                WHEN COALESCE(hardDeadline, softDeadline) IS NOT NULL
                     AND COALESCE(hardDeadline, softDeadline) < :now THEN 0
                WHEN COALESCE(hardDeadline, softDeadline) IS NOT NULL THEN 1
                ELSE 2
            END,
            COALESCE(hardDeadline, softDeadline) ASC
    """)
    fun observeActiveTasks(now: Long): Flow<List<TaskEntity>>

    // Recently completed tasks (within last 24 hours)
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 AND completedAt > :since ORDER BY completedAt DESC")
    fun observeRecentlyCompleted(since: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = 1, completedAt = :completedAt WHERE id = :id")
    suspend fun completeTask(id: String, completedAt: Long)

    @Query("UPDATE tasks SET isCompleted = 0, completedAt = NULL WHERE id = :id")
    suspend fun uncompleteTask(id: String)
}
