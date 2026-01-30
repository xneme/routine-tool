package com.routinetool.data.repository

import com.routinetool.data.local.database.SubtaskDao
import com.routinetool.data.local.database.TaskDao
import com.routinetool.data.local.entities.SubtaskEntity
import com.routinetool.data.local.entities.TaskEntity
import com.routinetool.domain.model.Subtask
import com.routinetool.domain.model.Task
import com.routinetool.domain.model.TaskWithSubtasks
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class TaskRepository(
    private val taskDao: TaskDao,
    private val subtaskDao: SubtaskDao
) {

    /**
     * Observe active (incomplete) tasks ordered by deadline proximity.
     * Overdue tasks appear first, followed by upcoming deadlines, then tasks with no deadline.
     */
    fun observeActiveTasks(): Flow<List<Task>> {
        val now = java.lang.System.currentTimeMillis()
        return taskDao.observeActiveTasks(now).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Observe recently completed tasks (within last 24 hours).
     */
    fun observeRecentlyCompleted(): Flow<List<Task>> {
        val oneDayAgo = java.lang.System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        return taskDao.observeRecentlyCompleted(oneDayAgo).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Observe active (incomplete) tasks with their subtasks.
     * Combines each task with its subtasks for display in task list.
     */
    fun observeActiveTasksWithSubtasks(): Flow<List<TaskWithSubtasks>> {
        return observeActiveTasks().flatMapLatest { tasks ->
            if (tasks.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    tasks.map { task ->
                        observeSubtasks(task.id).map { subtasks ->
                            TaskWithSubtasks(task, subtasks)
                        }
                    }
                ) { it.toList() }
            }
        }
    }

    /**
     * Observe recently completed tasks with their subtasks.
     * Combines each task with its subtasks for display in task list.
     */
    fun observeRecentlyCompletedWithSubtasks(): Flow<List<TaskWithSubtasks>> {
        return observeRecentlyCompleted().flatMapLatest { tasks ->
            if (tasks.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    tasks.map { task ->
                        observeSubtasks(task.id).map { subtasks ->
                            TaskWithSubtasks(task, subtasks)
                        }
                    }
                ) { it.toList() }
            }
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
        val completedAt = java.lang.System.currentTimeMillis()
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

    // ========== Subtask Operations ==========

    /**
     * Observe subtasks for a specific task, ordered by position.
     */
    fun observeSubtasks(taskId: String): Flow<List<Subtask>> {
        return subtaskDao.observeSubtasksByTaskId(taskId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Add a new subtask to a task. Automatically assigns the next position.
     */
    suspend fun addSubtask(taskId: String, title: String) {
        val existingSubtasks = subtaskDao.getSubtasksByTaskId(taskId)
        val nextPosition = if (existingSubtasks.isEmpty()) {
            0f
        } else {
            existingSubtasks.maxOf { it.position } + 1f
        }

        val subtask = SubtaskEntity(
            id = UUID.randomUUID().toString(),
            taskId = taskId,
            title = title,
            position = nextPosition
        )
        subtaskDao.insert(subtask)
    }

    /**
     * Toggle subtask completion state.
     * If completed, mark as incomplete. If incomplete, mark as complete with current timestamp.
     */
    suspend fun toggleSubtask(subtaskId: String) {
        val subtask = subtaskDao.getById(subtaskId) ?: return

        if (subtask.isCompleted) {
            subtaskDao.uncompleteSubtask(subtaskId)
        } else {
            val completedAt = java.lang.System.currentTimeMillis()
            subtaskDao.completeSubtask(subtaskId, completedAt)
        }
    }

    /**
     * Delete a subtask.
     */
    suspend fun deleteSubtask(subtaskId: String) {
        val subtask = subtaskDao.getById(subtaskId) ?: return
        subtaskDao.delete(subtask)
    }

    /**
     * Reorder a subtask using fractional indexing.
     * Calculates new position based on surrounding subtasks and handles precision loss.
     */
    suspend fun reorderSubtask(subtaskId: String, newIndex: Int, allSubtasks: List<Subtask>) {
        if (newIndex < 0 || newIndex >= allSubtasks.size) return

        val newPosition = when {
            // Moving to first position
            newIndex == 0 -> {
                if (allSubtasks.isEmpty()) 0f
                else allSubtasks.first().position - 1f
            }
            // Moving to last position
            newIndex >= allSubtasks.size - 1 -> {
                allSubtasks.last().position + 1f
            }
            // Moving to middle position (fractional indexing)
            else -> {
                val prevPosition = allSubtasks[newIndex - 1].position
                val nextPosition = allSubtasks[newIndex].position
                val calculatedPosition = (prevPosition + nextPosition) / 2f

                // Check for precision loss (gap too small)
                if (nextPosition - prevPosition < 0.0001f) {
                    // Need to renumber all subtasks
                    renumberSubtasks(allSubtasks)
                    // After renumbering, use simple calculation
                    newIndex.toFloat()
                } else {
                    calculatedPosition
                }
            }
        }

        subtaskDao.updatePosition(subtaskId, newPosition)
    }

    /**
     * Renumber all subtasks with integer positions when precision is lost.
     * Private helper for reorderSubtask.
     */
    private suspend fun renumberSubtasks(subtasks: List<Subtask>) {
        subtasks.forEachIndexed { index, subtask ->
            subtaskDao.updatePosition(subtask.id, index.toFloat())
        }
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

/**
 * Extension function to convert SubtaskEntity (epoch millis) to Subtask (Instant).
 */
private fun SubtaskEntity.toDomainModel(): Subtask = Subtask(
    id = id,
    taskId = taskId,
    title = title,
    isCompleted = isCompleted,
    completedAt = completedAt?.let { Instant.fromEpochMilliseconds(it) },
    position = position,
    createdAt = Instant.fromEpochMilliseconds(createdAt)
)

/**
 * Extension function to convert Subtask (Instant) to SubtaskEntity (epoch millis).
 */
private fun Subtask.toEntity(): SubtaskEntity = SubtaskEntity(
    id = id,
    taskId = taskId,
    title = title,
    isCompleted = isCompleted,
    completedAt = completedAt?.toEpochMilliseconds(),
    position = position,
    createdAt = createdAt.toEpochMilliseconds()
)
