package ru.coursefinder.data.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.coursefinder.data.local.CoursesDatabase
import ru.coursefinder.data.remote.coursesApiModule
import ru.coursefinder.data.repository.CoursesRepositoryImpl
import ru.coursefinder.domain.repository.CoursesRepository

val dataModule = module {
    single<HttpClient> { HttpClient(Android) }

    includes(coursesApiModule)

    single<CoursesDatabase> { CoursesDatabase.getDatabase(androidContext()) }

    single<CoursesRepository> {
        CoursesRepositoryImpl(
            coursesDb = get(),
            api = get()
        )
    }
}