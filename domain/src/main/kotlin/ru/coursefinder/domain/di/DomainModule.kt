package ru.coursefinder.domain.di

import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import ru.coursefinder.domain.usecase.GetAvailableCoursesUseCase
import ru.coursefinder.domain.usecase.GetAvailableCoursesUseCaseImpl

val domainModule = module {
    single<GetAvailableCoursesUseCase> {
        GetAvailableCoursesUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }
}