package com.example.intellipaat.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.intellipaat.domain.CourseEntity
import com.example.intellipaat.domain.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Query("UPDATE courses SET progress = :progress WHERE id = :courseId")
    suspend fun updateCourseProgress(courseId: Int, progress: Int)

}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE courseId = :courseId")
    fun getLessonsForCourse(courseId: Int): Flow<List<LessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("UPDATE lessons SET isCompleted = :isCompleted WHERE id = :lessonId")
    suspend fun updateLessonStatus(lessonId: String, isCompleted: Boolean)

}

@Database(entities = [CourseEntity::class, LessonEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
}

