package ru.coursefinder.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ru.coursefinder.data.remote.CoursesApi
import ru.coursefinder.data.remote.CoursesApiPagingSource
import ru.coursefinder.domain.model.Course
import ru.coursefinder.domain.repository.CoursesRepository

internal class CoursesRepositoryImpl(private val api: CoursesApi) : CoursesRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getAvailableCourses(): Flow<PagingData<Course>> {
        return Pager(
            pagingSourceFactory = { CoursesApiPagingSource(api) },
            config = PagingConfig(
                initialLoadSize = 10,
                pageSize = 5,
                prefetchDistance = 5,
                enablePlaceholders = false
            )
        ).flow
    }


    override suspend fun getCourseById(id: Long): Result<Course> {
        TODO("Not yet implemented")
    }


    override suspend fun likeCourse(course: Course): Result<Unit> {
        TODO("Not yet implemented")
    }


    override suspend fun dislikeCourse(course: Course): Result<Unit> {
        TODO("Not yet implemented")
    }


    override fun getFavouriteCourses(): Flow<List<Course>> {
        return emptyFlow()
    }
}