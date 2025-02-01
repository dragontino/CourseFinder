package ru.coursefinder.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.coursefinder.domain.model.Course
import ru.coursefinder.domain.model.User

@Serializable
internal data class CourseDto(
    val id: Long,
    val title: String,
    @SerialName("title_en") val englishTitle: String,
    val summary: String,
    val description: String,
    val workload: String,
    val cover: String,
    val intro: String,
    @SerialName("course_format") val courseFormat: String,
    @SerialName("target_audience") val targetAudience: String,
    val requirements: String,
    @SerialName("became_published_at") val publishDate: String,
    @SerialName("continue_url") val startCourseUrl: String,
    @SerialName("canonical_url") val canonicalUrl: String,
    @SerialName("display_price") val price: String?,
    @SerialName("is_favorite") val isFavourite: Boolean,
    val authorIds: List<Long> = emptyList()
) {
    fun convertToDomainCourse(
        authors: List<User>,
        rating: Double?
    ) = Course(
        id = id,
        title = title,
        englishTitle = englishTitle,
        summary = summary,
        description = description,
        workload = workload,
        cover = cover,
        isFavourite = isFavourite,
        intro = intro,
        courseFormat = courseFormat,
        targetAudience = targetAudience,
        requirements = requirements,
        publishDate = publishDate,
        startCourseUrl = startCourseUrl,
        canonicalUrl = canonicalUrl,
        price = price,
        rating = rating,
        authors = authors,
    )
}
