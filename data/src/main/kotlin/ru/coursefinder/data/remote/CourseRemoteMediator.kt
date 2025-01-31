package ru.coursefinder.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import ru.coursefinder.data.model.CoursesResponse

@OptIn(ExperimentalPagingApi::class)
internal class CourseRemoteMediator(
    private val coursesApi: CoursesApi
) : RemoteMediator<Int, CoursesResponse>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, CoursesResponse>): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.REFRESH -> 1
                LoadType.APPEND -> state.lastItemOrNull()?.meta?.nextPage ?: 1
            }

            val coursesResponse = coursesApi.getCoursesFromPage(
                page = loadKey,
                pageSize = state.config.pageSize
            )
            MediatorResult.Success(endOfPaginationReached = coursesResponse.meta.hasNext.not())

        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}