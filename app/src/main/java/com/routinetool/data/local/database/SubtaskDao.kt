package com.routinetool.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.routinetool.data.local.entities.SubtaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subtask: SubtaskEntity)

    @Update
    suspend fun update(subtask: SubtaskEntity)

    @Delete
    suspend fun delete(subtask: SubtaskEntity)

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY position ASC")
    suspend fun getSubtasksByTaskId(taskId: String): List<SubtaskEntity>

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY position ASC")
    fun observeSubtasksByTaskId(taskId: String): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks WHERE id = :id")
    suspend fun getById(id: String): SubtaskEntity?

    @Query("UPDATE subtasks SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: String, position: Float)

    @Query("UPDATE subtasks SET isCompleted = 1, completedAt = :completedAt WHERE id = :id")
    suspend fun completeSubtask(id: String, completedAt: Long)

    @Query("UPDATE subtasks SET isCompleted = 0, completedAt = NULL WHERE id = :id")
    suspend fun uncompleteSubtask(id: String)
}
