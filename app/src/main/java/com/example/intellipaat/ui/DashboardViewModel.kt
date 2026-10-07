package com.example.intellipaat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intellipaat.domain.CourseEntity
import com.example.intellipaat.domain.LessonEntity
import com.example.intellipaat.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    object Empty : DashboardUiState
    object Loading : DashboardUiState
    data class Error(val message: String) : DashboardUiState
    data class Success(val courses: List<CourseEntity>) : DashboardUiState
}

class DashboardViewModel(private val courseRepository: CourseRepository) : ViewModel()  {

    val uiState: StateFlow<DashboardUiState> = courseRepository.getCourses().map {
        courses ->
        if(courses.isEmpty()) DashboardUiState.Empty else DashboardUiState.Success(courses)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState.Loading)

    init {
        fetchRemoteData()
    }

    fun fetchRemoteData() {
        viewModelScope.launch {
            try {
                courseRepository.refreshCourses()
            }
            catch (_: Exception) {

            }
        }
    }

    fun getLessonsForCourse(courseId: Int) : Flow<List<LessonEntity>> = courseRepository.getLessons(courseId)

    fun toggleLesson(courseId: Int, lessonId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            courseRepository.toggleLessonCompletion(courseId, lessonId, isCompleted)
        }
    }
}