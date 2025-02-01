package ru.coursefinder.domain.usecase

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.coursefinder.domain.repository.CoursesRepository

interface RemoveCourseUseCase {
    suspend operator fun invoke(id: Long): Result<Unit>
}

internal class RemoveCourseUseCaseImpl(
    private val repository: CoursesRepository,
    private val dispatcher: CoroutineDispatcher
) : RemoveCourseUseCase {
    private companion object {
        const val TAG = "RemoveCourseUseCase"
    }

    override suspend fun invoke(id: Long): Result<Unit> {
        return withContext(dispatcher) {
            repository.removeCourseFromSaved(id).onFailure {
                Log.e(TAG, it.message, it)
            }
        }
    }
}