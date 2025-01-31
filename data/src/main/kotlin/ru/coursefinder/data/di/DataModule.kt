package ru.coursefinder.data.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import org.koin.dsl.module
import ru.coursefinder.data.remote.coursesApiModule
import ru.coursefinder.data.repository.CoursesRepositoryImpl
import ru.coursefinder.domain.repository.CoursesRepository

val dataModule = module {
    single<HttpClient> { HttpClient(Android) }
    single<CoursesRepository> { CoursesRepositoryImpl(get()) }
    includes(coursesApiModule)
}