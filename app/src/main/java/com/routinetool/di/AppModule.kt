package com.routinetool.di

import androidx.room.Room
import com.routinetool.data.local.database.AppDatabase
import com.routinetool.data.local.database.MIGRATION_1_2
import com.routinetool.data.preferences.PreferencesDataStore
import com.routinetool.data.repository.TaskRepository
import com.routinetool.ui.screens.addtask.AddTaskViewModel
import com.routinetool.ui.screens.focus.FocusViewModel
import com.routinetool.ui.screens.tasklist.TaskListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "routine_tool_db"
        )
        .addMigrations(MIGRATION_1_2)
        .build()
    }

    // DAO
    single { get<AppDatabase>().taskDao() }
    single { get<AppDatabase>().subtaskDao() }

    // Repository
    single { TaskRepository(get()) }

    // Preferences DataStore
    single { PreferencesDataStore(androidContext()) }

    // ViewModels
    viewModel { TaskListViewModel(get(), get()) }
    viewModel { params -> AddTaskViewModel(get(), get(), params.getOrNull()) }
    viewModel { FocusViewModel(get(), get()) }
}
