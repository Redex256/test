package com.santtuhyvarinen.habittracker.managers

import androidx.lifecycle.MutableLiveData
import com.santtuhyvarinen.habittracker.database.PlantStateEntity
import com.santtuhyvarinen.habittracker.models.HabitWithTaskLogs
import com.santtuhyvarinen.habittracker.models.TaskLog
import com.santtuhyvarinen.habittracker.models.TaskModel
import com.santtuhyvarinen.habittracker.utils.CalendarUtil
import com.santtuhyvarinen.habittracker.utils.TaskUtil

class TaskManager(private val databaseManager: DatabaseManager) {

    val tasks : MutableLiveData<ArrayList<TaskModel>> by lazy {
        MutableLiveData<ArrayList<TaskModel>>()
    }

    fun generateDailyTasks(habits : List<HabitWithTaskLogs>) {
        val taskList = ArrayList<TaskModel>()
        val currentTimestamp = System.currentTimeMillis()

        for(habitWithTaskLogs in habits) {
            if (habitWithTaskLogs.habit.disabled) continue

            if (CalendarUtil.isHabitScheduledForToday(habitWithTaskLogs.habit)) {

                // Якщо є лог за сьогодні — не додаємо задачу
                if (!TaskUtil.hasTaskLogForDate(habitWithTaskLogs, currentTimestamp)) {
                    taskList.add(TaskModel(habitWithTaskLogs))
                }
            }
        }

        tasks.value = taskList
    }


    // -------------------------------------------------------------------------
    //                        ВИКОНАННЯ ЗВИЧКИ
    //                        ДОДАЄ 1 БАЛ РОСЛИНІ
    // -------------------------------------------------------------------------
    suspend fun insertTaskLog(taskModel: TaskModel, taskStatus : String, timestamp: Long) {
        val taskLog = TaskLog()
        val habit = taskModel.habitWithTaskLogs.habit

        taskLog.habitId = habit.id
        taskLog.timestamp = timestamp
        taskLog.status = taskStatus

        when (taskStatus) {

            TaskUtil.STATUS_SUCCESS -> {
                val newScore = habit.score + 1
                habit.score = newScore
                taskLog.score = newScore

                // -----------------------------
                // ✔ ДОДАЄМО БАЛ РОСЛИНІ
                // -----------------------------
                val plantDao = databaseManager.plantStateDao

                val currentState =
                    plantDao.getState()
                        ?: PlantStateEntity(
                            id = 1,      // ВАЖЛИВО: той самий ID, що читає DAO
                            points = 0,
                            growthLevel = 0f
                        )

                val updatedState = currentState.copy(
                    points = currentState.points + 1   // +1 бал за звичку
                )

                plantDao.saveState(updatedState)
            }

            TaskUtil.STATUS_FAILED -> {
                habit.score = 0
                taskLog.score = 0
            }
        }

        databaseManager.taskLogRepository.createTaskLog(taskLog)
        databaseManager.habitRepository.updateHabit(habit)
    }
}
