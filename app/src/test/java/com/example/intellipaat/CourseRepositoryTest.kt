package com.example.intellipaat

import com.example.intellipaat.data.CourseDao
import com.example.intellipaat.data.LessonDao
import com.example.intellipaat.domain.LessonEntity
import com.example.intellipaat.repository.CourseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CourseRepositoryTest {

    private val courseDao = mockk<CourseDao>(relaxed = true)
    private val lessonDao = mockk<LessonDao>(relaxed = true)
    private val repository = CourseRepository(courseDao, lessonDao)

    @Test
    fun `toggleLessonCompletion updates course progress correctly`() = runTest {
        // Arrange
        val courseId = 1
        val mockLessons = listOf(
            LessonEntity("1_1", courseId, "Intro", true),
            LessonEntity("1_2", courseId, "Variables", false) // 1 out of 2 completed = 50%
        )

        coEvery { lessonDao.getLessonsForCourse(courseId) } returns flowOf(mockLessons)

        // Act
        repository.toggleLessonCompletion(courseId, "1_2", true)

        // Assert
        coVerify { courseDao.updateCourseProgress(courseId, 50) }
    }
}