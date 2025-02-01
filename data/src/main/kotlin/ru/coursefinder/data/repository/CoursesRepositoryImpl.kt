package ru.coursefinder.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import ru.coursefinder.data.remote.CoursesApi
import ru.coursefinder.data.remote.CoursesApiPagingSource
import ru.coursefinder.domain.model.Course
import ru.coursefinder.domain.repository.CoursesRepository

internal class CoursesRepositoryImpl(private val api: CoursesApi) : CoursesRepository {

    private companion object {
        const val TAG = "CoursesRepository"
    }


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
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, throwable.message, throwable)
        }

        return runCatching {
            val courseDto = api.getCourseById(id).courses[0]
            return withContext(exceptionHandler) {
                val courseRatingAsync = async { api.getCourseRating(courseDto.id) }
                val authors = courseDto.authorIds.map {
                    async { api.getUserById(it).users[0] }
                }

                val course = courseDto.convertToDomainCourse(
                    authors = authors.awaitAll(),
                    rating = courseRatingAsync.await()
                )
                return@withContext Result.success(course)
            }
        }
    }


    override suspend fun saveCourse(courseId: Long): Result<Unit> {
        return Result.success(Unit)
    }


    override suspend fun removeCourseFromSaved(courseId: Long): Result<Unit> {
        return Result.success(Unit)
    }


    override fun getSavedCourses(): Flow<List<Course>> {
        return emptyFlow()
    }
}