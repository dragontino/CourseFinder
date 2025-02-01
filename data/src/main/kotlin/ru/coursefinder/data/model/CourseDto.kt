package ru.coursefinder.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerialName("learners_count") val learnersCount: Int,
    val authorIds: List<Long> = emptyList()
)
