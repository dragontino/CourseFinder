package ru.coursefinder.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    @SerialName("full_name") val fullName: String,
    @SerialName("avatar") val avatarUrl: String
)
