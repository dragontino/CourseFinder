package ru.coursefinder.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import ru.coursefinder.app.di.appModule
import ru.coursefinder.data.di.dataModule
import ru.coursefinder.domain.di.domainModule

class CourseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CourseApplication)
            slf4jLogger()
            modules(domainModule, dataModule, appModule)
        }
    }
}