package ru.coursefinder.data.remote

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import ru.coursefinder.data.local.CoursesDatabase
import ru.coursefinder.data.model.CourseEntity
import ru.coursefinder.data.model.mapToCourseEntity

@OptIn(ExperimentalPagingApi::class)
internal class CourseRemoteMediator(
    private val coursesDb: CoursesDatabase,
    private val coursesApi: CoursesApi
) : RemoteMediator<Int, CourseEntity>() {

    private companion object {
        const val INITIAL_PAGE = 2
        const val TAG = "CoursesRemoteMediator"
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, CourseEntity>): MediatorResult {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, throwable.message, throwable)
        }

        val result: Result<MediatorResult> = runCatching {
            val currentPage = when (loadType) {
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.REFRESH -> INITIAL_PAGE
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull() ?: return MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                    lastItem.page
                }
            }

            val coursesResponse = coursesApi.getCoursesFromPage(
                page = currentPage,
                pageSize = state.config.pageSize
            )

            Log.d(
                TAG,
                "Loaded ${coursesResponse.courses.size} courses from page ${coursesResponse.meta.page}"
            )
            withContext(exceptionHandler) {
                val courses = coursesResponse.courses.map { courseDto ->
                    val ratingAsync = async { coursesApi.getCourseRating(courseDto.id) }
                    val authorsAsync = courseDto.authorIds.map { authorId ->
                        async(Dispatchers.IO) { coursesApi.getUserById(authorId).users[0] }
                    }
                    courseDto
                        .copy(price = courseDto.price.takeUnless { it == "-" })
                        .mapToCourseEntity(
                            authors = authorsAsync.awaitAll(),
                            rating = ratingAsync.await(),
                            page = coursesResponse.meta.page
                        )
                }

                coursesDb.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        coursesDb.dao.clearUnsavedCourses()
                    }
                    coursesDb.dao.upsertAll(courses)
                }
            }

            MediatorResult.Success(
                endOfPaginationReached = coursesResponse.meta.hasNext.not()
            )
        }.onFailure {
            Log.e(TAG, it.message, it)
        }

        return result.getOrNull()
            ?: result.exceptionOrNull()!!.let(MediatorResult::Error)
    }
}