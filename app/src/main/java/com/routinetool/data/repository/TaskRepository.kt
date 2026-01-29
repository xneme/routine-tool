package com.routinetool.data.repository

import com.routinetool.data.local.database.TaskDao
import com.routinetool.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

class TaskRepository(private val taskDao: TaskDao) {

    /**
     * Observe active (incomplete) tasks ordered by deadline proximity.
     * Overdue tasks appear first, followed by upcoming deadlines, then tasks with no deadline.
     */
    fun observeActiveTasks(): Flow<List<TaskEntity>> {
        val now = Clock.System.now().toEpochMilliseconds()
        return taskDao.observeActiveTasks(now)
    }

    /**
     * Observe recently completed tasks (within last 24 hours).
     */
    fun observeRecentlyCompleted(): Flow<List<TaskEntity>> {
        val oneDayAgo = Clock.System.now().toEpochMilliseconds() - (24 * 60 * 60 * 1000)
        return taskDao.observeRecentlyCompleted(oneDayAgo)
    }

    /**
     * Get a single task by ID.
     */
    suspend fun getById(id: String): TaskEntity? {
        return taskDao.getById(id)
    }

    /**
     * Insert a new task or replace existing.
     */
    suspend fun insert(task: TaskEntity) {
        taskDao.insert(task)
    }

    /**
     * Update an existing task.
     */
    suspend fun update(task: TaskEntity) {
        taskDao.update(task)
    }

    /**
     * Mark a task as completed with current timestamp.
     */
    suspend fun completeTask(id: String) {
        val completedAt = Clock.System.now().toEpochMilliseconds()
        taskDao.completeTask(id, completedAt)
    }

    /**
     * Mark a task as incomplete (uncomplete).
     */
    suspend fun uncompleteTask(id: String) {
        taskDao.uncompleteTask(id)
    }
}
