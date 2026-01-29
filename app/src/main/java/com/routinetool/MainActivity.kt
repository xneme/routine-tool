package com.routinetool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.routinetool.ui.screens.tasklist.TaskListScreen
import com.routinetool.ui.theme.RoutineToolTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoutineToolTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Temporary - navigation will be added in Plan 03
                    TaskListScreen(
                        onAddTask = { /* Will be wired in Plan 03 */ }
                    )
                }
            }
        }
    }
}
