package ru.coursefinder.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.coursefinder.app.utils.Action
import ru.coursefinder.app.utils.EventMessage
import ru.coursefinder.domain.model.Course
import ru.coursefinder.domain.usecase.GetAvailableCoursesUseCase

class HomeViewModel(private val coursesUseCase: GetAvailableCoursesUseCase) : ViewModel() {
    private val messageChannel = Channel<EventMessage>()
    val messageLiveData = messageChannel.receiveAsFlow().asLiveData()

    val coursesFlow: StateFlow<PagingData<Course>> by lazy {
        coursesUseCase()
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
}
