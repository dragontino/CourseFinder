package ru.coursefinder.app.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.coursefinder.app.ui.home.HomeViewModel

val appModule = module {
    viewModelOf(::HomeViewModel)
}