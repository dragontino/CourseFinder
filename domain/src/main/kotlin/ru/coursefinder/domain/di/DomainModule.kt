package ru.coursefinder.domain.di

import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import ru.coursefinder.domain.usecase.GetAvailableCoursesUseCase
import ru.coursefinder.domain.usecase.GetAvailableCoursesUseCaseImpl
import ru.coursefinder.domain.usecase.GetCourseByIdUseCase
import ru.coursefinder.domain.usecase.GetCourseByIdUseCaseImpl
import ru.coursefinder.domain.usecase.RemoveCourseUseCase
import ru.coursefinder.domain.usecase.RemoveCourseUseCaseImpl
import ru.coursefinder.domain.usecase.SaveCourseUseCase
import ru.coursefinder.domain.usecase.SaveCourseUseCaseImpl

val domainModule = module {
    single<GetAvailableCoursesUseCase> {
        GetAvailableCoursesUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<GetCourseByIdUseCase> {
        GetCourseByIdUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<SaveCourseUseCase> {
        SaveCourseUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single<RemoveCourseUseCase> {
        RemoveCourseUseCaseImpl(
            repository = get(),
            dispatcher = Dispatchers.IO
        )
    }
}