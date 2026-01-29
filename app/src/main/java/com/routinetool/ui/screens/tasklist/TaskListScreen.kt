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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.routinetool.domain.model.Task
import com.routinetool.ui.components.TaskCard
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main task list screen with sectioned layout.
 * Sections: Overdue → Active → Done
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        topBar = {
            TopAppBar(
                title = { Text("Routine Tool") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
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
                            style = MaterialTheme.typography.labelLarge,
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
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // Active tasks section (no header - this is the default view)
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

                // Done section
                if (uiState.doneTasks.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelLarge,
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
                            isLongOverdue = false,
                            modifier = Modifier.alpha(0.6f)
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
    val now = Clock.System.now()
    val daysOverdue = (now - nearestDeadline).inWholeDays
    return daysOverdue >= TaskListViewModel.LONG_OVERDUE_DAYS
}
