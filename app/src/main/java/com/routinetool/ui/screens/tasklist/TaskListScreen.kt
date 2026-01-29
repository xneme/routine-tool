package com.routinetool.ui.screens.tasklist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.routinetool.domain.model.Task
import com.routinetool.ui.components.TaskCard
import kotlin.time.Duration.Companion.milliseconds
import org.koin.androidx.compose.koinViewModel

/**
 * Main task list screen with sectioned layout.
 * Sections: Overdue → Active → Done
 */
@Composable
fun TaskListScreen(
    onAddTask: () -> Unit,
    onEditTask: (String) -> Unit = {},
    viewModel: TaskListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Track expanded task ID locally in the composable
    var expandedTaskId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add task"
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (
            uiState.overdueTasks.isEmpty() &&
            uiState.activeTasks.isEmpty() &&
            uiState.doneTasks.isEmpty()
        ) {
            // Empty state
            EmptyState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Overdue section
                if (uiState.overdueTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Overdue",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(uiState.overdueTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            isExpanded = expandedTaskId == task.id,
                            onExpandToggle = {
                                expandedTaskId = if (expandedTaskId == task.id) null else task.id
                            },
                            onComplete = { viewModel.completeTask(task.id) },
                            onUncomplete = { viewModel.uncompleteTask(task.id) },
                            onDismissOverdue = { viewModel.dismissOverdue(task.id) },
                            onReschedule = { newDate -> viewModel.rescheduleTask(task.id, newDate) },
                            onEditTask = { onEditTask(task.id) },
                            isOverdue = true,
                            isLongOverdue = isLongOverdue(task)
                        )
                    }

                    // Divider after Overdue if there's content following
                    if (uiState.activeTasks.isNotEmpty() || uiState.doneTasks.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }
                }

                // Active tasks section with "Tasks" header
                if (uiState.activeTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Tasks",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(uiState.activeTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            isExpanded = expandedTaskId == task.id,
                            onExpandToggle = {
                                expandedTaskId = if (expandedTaskId == task.id) null else task.id
                            },
                            onComplete = { viewModel.completeTask(task.id) },
                            onUncomplete = { viewModel.uncompleteTask(task.id) },
                            onDismissOverdue = { viewModel.dismissOverdue(task.id) },
                            onReschedule = { newDate -> viewModel.rescheduleTask(task.id, newDate) },
                            onEditTask = { onEditTask(task.id) },
                            isOverdue = false,
                            isLongOverdue = false
                        )
                    }

                    // Divider after Active if Done section follows
                    if (uiState.doneTasks.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }
                }

                // Done section
                if (uiState.doneTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(uiState.doneTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            isExpanded = expandedTaskId == task.id,
                            onExpandToggle = {
                                expandedTaskId = if (expandedTaskId == task.id) null else task.id
                            },
                            onComplete = { viewModel.completeTask(task.id) },
                            onUncomplete = { viewModel.uncompleteTask(task.id) },
                            onDismissOverdue = { viewModel.dismissOverdue(task.id) },
                            onReschedule = { newDate -> viewModel.rescheduleTask(task.id, newDate) },
                            onEditTask = { onEditTask(task.id) },
                            isOverdue = false,
                            isLongOverdue = false
                        )
                    }
                }
            }
        }
    }
}

/**
 * Empty state shown when no tasks exist.
 */
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Nothing here yet",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Check if a task is long overdue (7+ days).
 */
private fun isLongOverdue(task: Task): Boolean {
    val nearestDeadline = listOfNotNull(task.softDeadline, task.hardDeadline).minOrNull() ?: return false
    val nowMillis = java.lang.System.currentTimeMillis()
    val deadlineMillis = nearestDeadline.toEpochMilliseconds()
    val durationMillis = nowMillis - deadlineMillis
    val daysOverdue = durationMillis.milliseconds.inWholeDays
    return daysOverdue >= TaskListViewModel.LONG_OVERDUE_DAYS
}
