package com.routinetool.ui.screens.tasklist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.routinetool.domain.model.FilterState
import com.routinetool.domain.model.SortOption
import com.routinetool.domain.model.Task
import com.routinetool.ui.components.TaskCard
import kotlin.time.Duration.Companion.milliseconds
import org.koin.androidx.compose.koinViewModel

/**
 * Main task list screen with sectioned layout.
 * Sections: Overdue -> Active -> Done
 * Features: Sort dropdown, filter chips
 */
@Composable
fun TaskListScreen(
    onAddTask: () -> Unit,
    onEditTask: (String) -> Unit = {},
    onNavigateToFocus: () -> Unit = {},
    viewModel: TaskListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    // Track expanded task ID locally in the composable
    var expandedTaskId by remember { mutableStateOf<String?>(null) }

    // Track FAB menu expansion state
    var fabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Secondary action: Focus View (shown when FAB expanded)
                AnimatedVisibility(visible = fabExpanded) {
                    SmallFloatingActionButton(
                        onClick = {
                            fabExpanded = false
                            onNavigateToFocus()
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.CenterFocusStrong, "Focus")
                    }
                }
                // Secondary action: Add Task (shown when FAB expanded)
                AnimatedVisibility(visible = fabExpanded) {
                    SmallFloatingActionButton(
                        onClick = {
                            fabExpanded = false
                            onAddTask()
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Add, "Add task")
                    }
                }
                // Main FAB: toggles menu or directly adds task when not expanded
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded }
                ) {
                    Icon(
                        imageVector = if (fabExpanded) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = if (fabExpanded) "Close menu" else "Open menu"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sort and Filter controls row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                FilterDropdown(
                    filterState = filterState,
                    onFilterChange = { newFilter -> viewModel.updateFilter { newFilter } }
                )
                SortDropdown(
                    currentSort = uiState.currentSort,
                    onSortChange = { viewModel.setSortOption(it) }
                )
            }

            // Task list content
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
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
}

/**
 * Sort dropdown triggered by sort icon.
 */
@Composable
private fun SortDropdown(
    currentSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.displayName) },
                    onClick = {
                        onSortChange(option)
                        expanded = false
                    },
                    leadingIcon = if (currentSort == option) {
                        { Icon(Icons.Filled.Check, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}

/**
 * Filter dropdown triggered by filter icon.
 * Shows badge when filters are active.
 */
@Composable
private fun FilterDropdown(
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val activeCount = filterState.activeFilterCount

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            BadgedBox(
                badge = {
                    if (activeCount > 0) {
                        Badge { Text("$activeCount") }
                    }
                }
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter options")
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Status section header
            Text(
                text = "Status",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
            DropdownMenuItem(
                text = { Text("Active") },
                onClick = { onFilterChange(filterState.copy(showActive = !filterState.showActive)) },
                leadingIcon = if (filterState.showActive) {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else null
            )
            DropdownMenuItem(
                text = { Text("Overdue") },
                onClick = { onFilterChange(filterState.copy(showOverdue = !filterState.showOverdue)) },
                leadingIcon = if (filterState.showOverdue) {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else null
            )
            DropdownMenuItem(
                text = { Text("Done") },
                onClick = { onFilterChange(filterState.copy(showDone = !filterState.showDone)) },
                leadingIcon = if (filterState.showDone) {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else null
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Deadline type section header
            Text(
                text = "Deadline Type",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
            DropdownMenuItem(
                text = { Text("Soft deadline") },
                onClick = { onFilterChange(filterState.copy(showSoftDeadline = !filterState.showSoftDeadline)) },
                leadingIcon = if (filterState.showSoftDeadline) {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else null
            )
            DropdownMenuItem(
                text = { Text("Hard deadline") },
                onClick = { onFilterChange(filterState.copy(showHardDeadline = !filterState.showHardDeadline)) },
                leadingIcon = if (filterState.showHardDeadline) {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else null
            )
            DropdownMenuItem(
                text = { Text("No deadline") },
                onClick = { onFilterChange(filterState.copy(showNoDeadline = !filterState.showNoDeadline)) },
                leadingIcon = if (filterState.showNoDeadline) {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else null
            )

            // Clear filters option (only if filters active)
            if (activeCount > 0) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                DropdownMenuItem(
                    text = { Text("Clear filters") },
                    onClick = {
                        onFilterChange(FilterState())
                        expanded = false
                    }
                )
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
