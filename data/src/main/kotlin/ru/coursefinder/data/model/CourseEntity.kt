package ru.coursefinder.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.coursefinder.domain.model.User

@Entity(tableName = "Course")
data class CourseEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val englishTitle: String,
    val summary: String,
    val description: String,
    val workload: String,
    val cover: String,
    val intro: String,
    val courseFormat: String,
    val targetAudience: String,
    val requirements: String,
    val publishDate: String,
    val startCourseUrl: String,
    val canonicalUrl: String,
    val price: String?,
    val rating: Double?,
    @ColumnInfo("is_favourite") val isFavourite: Boolean,
    @ColumnInfo("learners_count", defaultValue = "null") val learnersCount: Int?,
    val authors: List<User>,
    val page: Int
)
