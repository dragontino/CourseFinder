package ru.coursefinder.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ru.coursefinder.data.model.CourseEntity

@Dao
internal interface CourseDao {
    @Upsert
    suspend fun upsertAll(courses: List<CourseEntity>)

    @Query("SELECT * FROM Course")
    fun getAllCourses(): PagingSource<Int, CourseEntity>

    @Query("SELECT * FROM Course WHERE is_favourite = 1")
    fun getSavedCourses(): PagingSource<Int, CourseEntity>

    @Query("SELECT * FROM Course WHERE id = :id")
    suspend fun getCourseById(id: Long): CourseEntity?

    @Query("UPDATE Course SET is_favourite = 1 WHERE id = :courseId")
    suspend fun saveCourse(courseId: Long): Int?

    @Query("UPDATE Course SET is_favourite = 0 WHERE id = :courseId")
    suspend fun removeCourseFromSaved(courseId: Long): Int?

    @Query("DELETE FROM Course WHERE is_favourite = 0")
    suspend fun clearUnsavedCourses()
}