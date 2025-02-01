package ru.coursefinder.domain.model

sealed interface OrderBy {
    val isAscending: Boolean

    data class Popularity(override val isAscending: Boolean) : OrderBy

    data class Rating(override val isAscending: Boolean) : OrderBy

    data class PublishDate(override val isAscending: Boolean) : OrderBy
}