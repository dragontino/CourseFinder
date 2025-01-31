package ru.coursefinder.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.coursefinder.domain.model.Course

interface CoursesRepository {

    fun getAvailableCourses(): Flow<PagingData<Course>>

    suspend fun getCourseById(id: Long): Result<Course>

    suspend fun likeCourse(course: Course): Result<Unit>

    suspend fun dislikeCourse(course: Course): Result<Unit>

    fun getFavouriteCourses(): Flow<List<Course>>
}