package ru.coursefinder.data.model

import kotlinx.serialization.Serializable
import ru.coursefinder.domain.model.User

@Serializable
internal data class UsersResponse(
    val meta: Meta,
    val users: List<User>
)
