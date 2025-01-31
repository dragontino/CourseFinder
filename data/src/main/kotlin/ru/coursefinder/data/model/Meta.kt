package ru.coursefinder.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Meta(
    val page: Int,
    @SerialName("has_next") val hasNext: Boolean,
    @SerialName("has_previous") val hasPrevious: Boolean
) {
    val previousPage: Int? get() = when {
        hasPrevious -> page - 1
        else -> null
    }

    val nextPage: Int? get() = when {
        hasNext -> page + 1
        else -> null
    }
}