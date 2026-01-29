package com.routinetool.data.repository

import com.routinetool.data.local.database.TaskDao
import com.routinetool.data.local.entities.TaskEntity
import com.routinetool.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class TaskRepository(private val taskDao: TaskDao) {

    /**
     * Observe active (incomplete) tasks ordered by deadline proximity.
     * Overdue tasks appear first, followed by upcoming deadlines, then tasks with no deadline.
     */
    fun observeActiveTasks(): Flow<List<Task>> {
        val now = Clock.System.now().toEpochMilliseconds()
        return taskDao.observeActiveTasks(now).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Observe recently completed tasks (within last 24 hours).
     */
    fun observeRecentlyCompleted(): Flow<List<Task>> {
        val oneDayAgo = Clock.System.now().toEpochMilliseconds() - (24 * 60 * 60 * 1000)
        return taskDao.observeRecentlyCompleted(oneDayAgo).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Get a single task by ID.
     */
    suspend fun getById(id: String): Task? {
        return taskDao.getById(id)?.toDomainModel()
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

    /**
     * Remove deadlines from a task (dismiss overdue state).
     */
    suspend fun dismissOverdue(id: String) {
        val task = taskDao.getById(id) ?: return
        taskDao.update(task.copy(softDeadline = null, hardDeadline = null))
    }

    /**
     * Reschedule a task by updating the nearest deadline to the new date.
     * Updates soft deadline if it exists, otherwise hard deadline.
     */
    suspend fun rescheduleTask(id: String, newDate: LocalDate) {
        val task = taskDao.getById(id) ?: return
        val newDeadlineEpochMillis = newDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

        // Update the deadline that was overdue (prefer soft, fall back to hard)
        val updated = if (task.softDeadline != null) {
            task.copy(softDeadline = newDeadlineEpochMillis)
        } else if (task.hardDeadline != null) {
            task.copy(hardDeadline = newDeadlineEpochMillis)
        } else {
            task // No deadline to reschedule
        }

        taskDao.update(updated)
    }
}

/**
 * Extension function to convert TaskEntity (epoch millis) to Task (Instant).
 */
private fun TaskEntity.toDomainModel(): Task = Task(
    id = id,
    title = title,
    description = description,
    softDeadline = softDeadline?.let { Instant.fromEpochMilliseconds(it) },
    hardDeadline = hardDeadline?.let { Instant.fromEpochMilliseconds(it) },
    isCompleted = isCompleted,
    completedAt = completedAt?.let { Instant.fromEpochMilliseconds(it) },
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    taskType = taskType
)
