package ru.coursefinder.data.model

import kotlinx.serialization.Serializable

@Serializable
internal data class CoursesResponse(
    val meta: Meta,
    val courses: List<CourseDto>
)
