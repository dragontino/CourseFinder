package ru.coursefinder.domain.usecase

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.coursefinder.domain.model.Course
import ru.coursefinder.domain.repository.CoursesRepository

interface GetCourseByIdUseCase {
    suspend operator fun invoke(courseId: Long): Result<Course>
}


internal class GetCourseByIdUseCaseImpl(
    private val repository: CoursesRepository,
    private val dispatcher: CoroutineDispatcher
) : GetCourseByIdUseCase {

    private companion object {
        const val TAG = "GetCourseByIdUseCase"
    }

    override suspend fun invoke(courseId: Long): Result<Course> {
        return withContext(dispatcher) {
            repository.getCourseById(courseId).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}