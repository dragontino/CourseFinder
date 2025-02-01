package ru.coursefinder.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.coursefinder.data.model.CourseEntity

@TypeConverters(UserListConverter::class)
@Database(
    entities = [CourseEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
internal abstract class CoursesDatabase : RoomDatabase() {
    abstract val dao: CourseDao

    companion object {
        @Volatile
        private var instance: CoursesDatabase? = null

        fun getDatabase(context: Context): CoursesDatabase {
            val temp = instance
            if (temp != null) {
                return temp
            }

            synchronized(this) {
                val database = Room.databaseBuilder(
                    context = context.applicationContext,
                    klass = CoursesDatabase::class.java,
                    name = "CachedCourses.db"
                ).build()

                instance = database
                return database
            }
        }
    }
}