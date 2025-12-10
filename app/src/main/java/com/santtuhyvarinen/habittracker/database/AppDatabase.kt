package com.santtuhyvarinen.habittracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.santtuhyvarinen.habittracker.database.dao.HabitDao
import com.santtuhyvarinen.habittracker.database.dao.PlantStateDao
import com.santtuhyvarinen.habittracker.database.dao.TaskLogDao
import com.santtuhyvarinen.habittracker.models.Habit
import com.santtuhyvarinen.habittracker.models.TaskLog

@Database(
    entities = [
        Habit::class,
        TaskLog::class,
        PlantStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun taskLogDao(): TaskLogDao
    abstract fun plantStateDao(): PlantStateDao

    companion object {

        const val DATABASE_LOG_TAG = "HabitTrackerDatabase"

        @Volatile
        private var DATABASE_INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return DATABASE_INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_database"
                ).build()

                DATABASE_INSTANCE = instance
                instance
            }
        }
    }
}
