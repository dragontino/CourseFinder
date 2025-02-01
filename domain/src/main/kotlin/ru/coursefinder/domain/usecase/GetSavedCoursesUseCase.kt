package ru.coursefinder.domain.usecase

import android.util.Log
import androidx.paging.PagingData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import ru.coursefinder.domain.model.Course
import ru.coursefinder.domain.model.OrderBy
import ru.coursefinder.domain.repository.CoursesRepository

interface GetSavedCoursesUseCase {
    operator fun invoke(orderBy: OrderBy? = null): Flow<PagingData<Course>>
}


internal class GetSavedCoursesUseCaseImpl(
    private val repository: CoursesRepository,
    private val dispatcher: CoroutineDispatcher
) : GetSavedCoursesUseCase {
    private companion object {
        const val TAG = "GetSavedCoursesUseCase"
    }

    override fun invoke(orderBy: OrderBy?): Flow<PagingData<Course>> {
        return repository
            .getSavedCourses(orderBy)
            .flowOn(dispatcher)
            .catch { throwable ->
                Log.e(TAG, throwable.message, throwable)
                throw throwable
            }
    }
}