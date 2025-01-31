package ru.coursefinder.app.utils

data class EventMessage(
    val text: String,
    val action: Action?
)


data class Action(
    val title: String,
    val actionBlock: () -> Unit
)
