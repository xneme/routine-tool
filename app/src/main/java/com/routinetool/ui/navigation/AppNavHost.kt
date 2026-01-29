package com.routinetool.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.routinetool.ui.screens.addtask.AddTaskScreen
import com.routinetool.ui.screens.focus.FocusViewScreen
import com.routinetool.ui.screens.tasklist.TaskListScreen

/**
 * Navigation graph for the app.
 * Defines all screens and navigation between them.
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.TASK_LIST
    ) {
        // Task list screen - landing page
        composable(NavRoutes.TASK_LIST) {
            TaskListScreen(
                onAddTask = { navController.navigate(NavRoutes.ADD_TASK) },
                onEditTask = { taskId -> navController.navigate(NavRoutes.editTask(taskId)) },
                onNavigateToFocus = { navController.navigate(NavRoutes.FOCUS_VIEW) }
            )
        }

        // Add new task screen
        composable(NavRoutes.ADD_TASK) {
            AddTaskScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Edit existing task screen with taskId parameter
        composable(
            route = NavRoutes.EDIT_TASK,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            AddTaskScreen(
                onNavigateBack = { navController.popBackStack() },
                taskId = taskId
            )
        }

        // Focus view screen - distraction-free task view
        composable(NavRoutes.FOCUS_VIEW) {
            FocusViewScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
