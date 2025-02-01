package ru.coursefinder.data.model

import ru.coursefinder.domain.model.Course
import ru.coursefinder.domain.model.User

fun CourseEntity.mapToDomainCourse() = Course(
    id = id,
    title = title,
    englishTitle = englishTitle,
    summary = summary,
    description = description,
    workload = workload,
    cover = cover,
    intro = intro,
    courseFormat = courseFormat,
    targetAudience = targetAudience,
    requirements = requirements,
    publishDate = publishDate,
    startCourseUrl = startCourseUrl,
    canonicalUrl = canonicalUrl,
    price = price,
    rating = rating,
    isFavourite = isFavourite,
    learnersCount = learnersCount,
    authors = authors
)


internal fun CourseDto.mapToCourseEntity(
    authors: List<User>,
    rating: Double?,
    page: Int
) = CourseEntity(
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
    startCourseUrl = "https://stepik.org$startCourseUrl",
    canonicalUrl = canonicalUrl,
    price = price,
    rating = rating,
    authors = authors,
    learnersCount = learnersCount,
    page = page
)