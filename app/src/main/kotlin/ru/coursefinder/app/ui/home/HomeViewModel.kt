package ru.coursefinder.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.coursefinder.app.utils.Action
import ru.coursefinder.app.utils.EventMessage
import ru.coursefinder.domain.model.Course
import ru.coursefinder.domain.model.OrderBy
import ru.coursefinder.domain.usecase.GetAvailableCoursesUseCase
import ru.coursefinder.domain.usecase.RemoveCourseUseCase
import ru.coursefinder.domain.usecase.SaveCourseUseCase

class HomeViewModel(
    private val coursesUseCase: GetAvailableCoursesUseCase,
    private val saveCourseUseCase: SaveCourseUseCase,
    private val removeCourseUseCase: RemoveCourseUseCase
) : ViewModel() {

    companion object {
        val defaultOrder = OrderBy.PublishDate(isAscending = false)
    }

    private val messageChannel = Channel<EventMessage>()
    val messageLiveData = messageChannel.receiveAsFlow().asLiveData()

    private val orderByStateFlow = MutableStateFlow<OrderBy>(defaultOrder)
    val orderByLiveData: LiveData<OrderBy> = orderByStateFlow.asLiveData()


    @OptIn(ExperimentalCoroutinesApi::class)
    val coursesFlow: StateFlow<PagingData<Course>> by lazy {
        orderByStateFlow.flatMapMerge { coursesUseCase(it) }
            .cachedIn(viewModelScope)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = PagingData.empty()
            )
    }

    fun sendMessage(message: String, action: Action? = null) {
        viewModelScope.launch {
            messageChannel.send(EventMessage(message, action))
        }
    }

    fun saveCourse(course: Course) {
        viewModelScope.launch {
            when {
                course.isFavourite -> removeCourseUseCase(course.id)
                else -> saveCourseUseCase(course.id)
            }.onFailure { throwable ->
                throwable.localizedMessage?.let { sendMessage(it) }
            }
        }
    }

    fun updateOrderBy(orderBy: OrderBy) {
        orderByStateFlow.update { orderBy }
    }
}
