package com.example.intellipaat.repository

import com.example.intellipaat.data.CourseDao
import com.example.intellipaat.data.LessonDao
import com.example.intellipaat.domain.CourseEntity
import com.example.intellipaat.domain.LessonEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CourseRepository (
    private val courseDao: CourseDao,
    private val lessonDao: LessonDao
) {

    fun getCourses(): Flow<List<CourseEntity>> = courseDao.getAllCourses()
    fun getLessons(courseId: Int): Flow<List<LessonEntity>> = lessonDao.getLessonsForCourse(courseId)

    suspend fun refreshCourses() {
        delay(1000)
        val mockCourses = listOf(
            CourseEntity(1, "Python Programming", "John Smith", 50, 2),
            CourseEntity(2, "Generative AI", "Sarah Williams", 0, 2),
            CourseEntity(3, "Full Stack Development", "David Brown", 0, 1)
        )

        val mockLessons = listOf(
            LessonEntity("1_1", 1, "Introduction", true),
            LessonEntity("1_2", 1, "Variables & Data Types", false),
            LessonEntity("2_1", 2, "LLM Basics", false),
            LessonEntity("2_2", 2, "Prompt Engineering", false),
            LessonEntity("3_1", 3, "HTML & CSS", false)
        )

        courseDao.insertCourses(mockCourses)
        lessonDao.insertLessons(mockLessons)
    }

    suspend fun toggleLessonCompletion(courseId: Int, lessonId: String, isCompleted: Boolean) {
        lessonDao.updateLessonStatus(lessonId, isCompleted)

        val lessons = lessonDao.getLessonsForCourse(courseId).first()
        val completedLessons = lessons.count { it.isCompleted }
        val newProgress =  if(lessons.isNotEmpty()) (completedLessons * 100) / lessons.size else 0

        courseDao.updateCourseProgress(courseId, newProgress)
    }

}