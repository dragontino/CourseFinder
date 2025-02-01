package ru.coursefinder.app.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.coursefinder.app.ui.course.CourseViewModel
import ru.coursefinder.app.ui.favourites.FavouriteCoursesViewModel
import ru.coursefinder.app.ui.home.HomeViewModel

val appModule = module {
    viewModelOf(::HomeViewModel)

    viewModelOf(::FavouriteCoursesViewModel)

    viewModel<CourseViewModel> { parameters ->
        CourseViewModel(
            courseId = parameters.get(),
            getCourseUseCase = get(),
            saveCourseUseCase = get(),
            removeCourseUseCase = get()
        )
    }
}