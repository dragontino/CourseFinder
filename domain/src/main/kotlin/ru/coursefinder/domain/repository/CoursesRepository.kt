package ru.coursefinder.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.coursefinder.domain.model.Course
import ru.coursefinder.domain.model.OrderBy

interface CoursesRepository {

    fun getAvailableCourses(orderBy: OrderBy?): Flow<PagingData<Course>>

    suspend fun getCourseById(id: Long): Result<Course>

    suspend fun saveCourse(courseId: Long): Result<Unit>

    suspend fun removeCourseFromSaved(courseId: Long): Result<Unit>

    fun getSavedCourses(orderBy: OrderBy?): Flow<PagingData<Course>>
}