package com.routinetool

import android.app.Application
import com.routinetool.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RoutineToolApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RoutineToolApp)
            modules(appModule)
        }
    }
}
