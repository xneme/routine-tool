package com.routinetool.di

import androidx.room.Room
import com.routinetool.data.local.database.AppDatabase
import com.routinetool.data.repository.TaskRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "routine_tool_db"
        ).build()
    }

    // DAO
    single { get<AppDatabase>().taskDao() }

    // Repository
    single { TaskRepository(get()) }

    // ViewModels will be added in subsequent plans
}
