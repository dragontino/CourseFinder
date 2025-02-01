package ru.coursefinder.domain.usecase

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.coursefinder.domain.repository.CoursesRepository

interface SaveCourseUseCase {
    suspend operator fun invoke(courseId: Long): Result<Unit>
}

internal class SaveCourseUseCaseImpl(
    private val repository: CoursesRepository,
    private val dispatcher: CoroutineDispatcher
) : SaveCourseUseCase {

    private companion object {
        const val TAG = "SaveCourseUseCase"
    }

    override suspend fun invoke(courseId: Long): Result<Unit> {
        return withContext(dispatcher) {
            repository.saveCourse(courseId).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}