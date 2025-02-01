package ru.coursefinder.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.coursefinder.data.local.CoursesDatabase
import ru.coursefinder.data.model.CourseEntity
import ru.coursefinder.data.model.mapToDomainCourse
import ru.coursefinder.data.remote.CourseRemoteMediator
import ru.coursefinder.data.remote.CoursesApi
import ru.coursefinder.domain.model.Course
import ru.coursefinder.domain.model.OrderBy
import ru.coursefinder.domain.repository.CoursesRepository

internal class CoursesRepositoryImpl(
    private val coursesDb: CoursesDatabase,
    private val api: CoursesApi
) : CoursesRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getAvailableCourses(orderBy: OrderBy?): Flow<PagingData<Course>> {
        val pager = getPager { coursesDb.dao.getCourses(getSortedListQuery(orderBy)) }
        return pager.flow.map { courseEntities ->
            courseEntities.map { it.mapToDomainCourse() }
        }
    }


    override suspend fun getCourseById(id: Long): Result<Course> {
        return coursesDb.dao.getCourseById(id)
            ?.let { Result.success(it.mapToDomainCourse()) }
            ?: Result.failure(Exception("Cannot find course with id $id"))
    }


    override suspend fun saveCourse(courseId: Long): Result<Unit> {
        return coursesDb.dao.saveCourse(courseId)
            ?.let { Result.success(Unit) }
            ?: Result.failure(Exception("Failed to save course with id $courseId"))
    }


    override suspend fun removeCourseFromSaved(courseId: Long): Result<Unit> {
        return coursesDb.dao.removeCourseFromSaved(courseId)
            ?.let { Result.success(Unit) }
            ?: Result.failure(Exception("Failed to remove course $courseId from the database"))
    }


    override fun getSavedCourses(orderBy: OrderBy?): Flow<PagingData<Course>> {
        val pager = getPager {
            val query = getSortedListQuery(orderBy, onlyFavourites = true)
            coursesDb.dao.getCourses(query)
        }
        return pager.flow.map { entities ->
            entities.map { it.mapToDomainCourse() }
        }
    }


    @OptIn(ExperimentalPagingApi::class)
    private fun getPager(
        sourceFactory: () -> PagingSource<Int, CourseEntity>
    ) = Pager(
        pagingSourceFactory = sourceFactory,
        remoteMediator = CourseRemoteMediator(coursesDb, api),
        config = PagingConfig(
            pageSize = 20,
            initialLoadSize = 5
        )
    )


    private fun getSortedListQuery(orderBy: OrderBy?, onlyFavourites: Boolean = false): SupportSQLiteQuery {
        val order = orderBy?.isAscending?.let { if (it) "ASC" else "DESC" }
        val columnName = when (orderBy) {
            is OrderBy.Popularity -> "learners_count"
            is OrderBy.PublishDate -> "publishDate"
            is OrderBy.Rating -> "rating"
            null -> null
        }
        val query = buildString {
            append("SELECT * FROM Course")
            if (onlyFavourites) append(" WHERE is_favourite = 1")
            if (order != null && columnName != null) {
                append(" ORDER BY $columnName $order")
            }
        }
        return SimpleSQLiteQuery(query)
    }
}