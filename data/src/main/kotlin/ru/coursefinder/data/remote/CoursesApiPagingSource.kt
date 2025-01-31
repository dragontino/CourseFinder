package ru.coursefinder.data.remote

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import ru.coursefinder.domain.model.Course

internal class CoursesApiPagingSource(private val api: CoursesApi) : PagingSource<Int, Course>() {

    private companion object {
        const val INITIAL_PAGE = 1
        const val MAX_PAGE = 30 // Временное ограничение
        const val TAG = "CoursesApiPagingSource"
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Course> {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, throwable.message, throwable)
        }

        return try {
            withContext(exceptionHandler) {
                val page = params.key ?: INITIAL_PAGE
                val response = api.getCoursesFromPage(page = page, pageSize = params.loadSize)
                Log.d(
                    "CoursesApiPagingSource",
                    "Loaded ${response.courses.size} courses from page ${response.meta.page}"
                )
                val courses = response.courses.map { course ->
                    val ratingAsync = async { api.getCourseRating(course.id) }
                    val authorsAsync = course.authorIds.map { authorId ->
                        async(Dispatchers.IO) { api.getUserById(authorId).users[0] }
                    }
                    course.convertToDomainCourse(
                        authors = authorsAsync.awaitAll(),
                        rating = ratingAsync.await()
                    )
                }

                LoadResult.Page(
                    data = courses,
                    prevKey = response.meta.previousPage,
                    nextKey = response.meta.nextPage?.takeIf { it <= MAX_PAGE }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            LoadResult.Error(e)
        }
    }


    override fun getRefreshKey(state: PagingState<Int, Course>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition).let { closedPage ->
                closedPage?.prevKey?.plus(1) ?: closedPage?.nextKey?.minus(1)
            }
        }
    }
}