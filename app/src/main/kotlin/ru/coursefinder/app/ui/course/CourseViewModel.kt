package ru.coursefinder.app.ui.course

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.coursefinder.app.utils.Action
import ru.coursefinder.app.utils.EventMessage
import ru.coursefinder.domain.model.Course
import ru.coursefinder.domain.usecase.GetCourseByIdUseCase
import ru.coursefinder.domain.usecase.RemoveCourseUseCase
import ru.coursefinder.domain.usecase.SaveCourseUseCase

class CourseViewModel(
    private val courseId: Long,
    private val getCourseUseCase: GetCourseByIdUseCase,
    private val saveCourseUseCase: SaveCourseUseCase,
    private val removeCourseUseCase: RemoveCourseUseCase

) : ViewModel() {
    private val messageChannel = Channel<EventMessage>()
    val messageLiveData = messageChannel.receiveAsFlow().asLiveData()

    private val _isLoadingLiveData = MutableLiveData(false)
    val isLoadingLiveData: LiveData<Boolean> = _isLoadingLiveData

    private var _courseLiveData = MutableLiveData<Course>()
    val courseLiveData: LiveData<Course> = _courseLiveData

    init {
        viewModelScope.launch {
            _isLoadingLiveData.postValue(true)
            getCourseUseCase.invoke(courseId)
                .onSuccess { _courseLiveData.postValue(it) }
                .onFailure { throwable ->
                    throwable.localizedMessage?.let { sendMessage(it) }
                }
            _isLoadingLiveData.postValue(false)
        }
    }


    fun saveCourse(course: Course) {
        viewModelScope.launch {
            _isLoadingLiveData.postValue(true)
            when {
                course.isFavourite -> removeCourseUseCase(course.id)
                else -> saveCourseUseCase(course.id)
            }.onFailure { throwable ->
                throwable.localizedMessage?.let { sendMessage(it) }
            }
            _isLoadingLiveData.postValue(false)
        }
    }


    fun sendMessage(message: String, action: Action? = null) {
        viewModelScope.launch {
            messageChannel.send(EventMessage(message, action))
        }
    }
}