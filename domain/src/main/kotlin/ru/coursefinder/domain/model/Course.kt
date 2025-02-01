package ru.coursefinder.domain.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Course(
    val id: Long,
    val title: String,
    val englishTitle: String,
    val summary: String,
    val description: String,
    val workload: String,
    val cover: String,
    val isFavourite: Boolean,
    val intro: String,
    val courseFormat: String,
    val targetAudience: String,
    val requirements: String,
    val publishDate: String,
    val startCourseUrl: String,
    val canonicalUrl: String,
    val price: String?,
    val rating: Double?,
    val learnersCount: Int?,
    val authors: List<User> = emptyList()
)
