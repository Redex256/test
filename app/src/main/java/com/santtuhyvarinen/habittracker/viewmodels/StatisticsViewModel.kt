package com.santtuhyvarinen.habittracker.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.santtuhyvarinen.habittracker.managers.DatabaseManager
import com.santtuhyvarinen.habittracker.models.HabitWithTaskLogs
import com.santtuhyvarinen.habittracker.models.ChartDataModel
import com.santtuhyvarinen.habittracker.database.PlantStateEntity
import com.santtuhyvarinen.habittracker.utils.TaskUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val databaseManager = DatabaseManager(application)
    private val plantDao = databaseManager.plantStateDao

    // ---------------- PLANT SYSTEM -------------------

    val plantPoints = MutableLiveData<Int>()
    val plantGrowth = MutableLiveData<Float>()

    init {
        loadPlantState()
    }

    private fun loadPlantState() {
        viewModelScope.launch(Dispatchers.IO) {

            var state = plantDao.getState()

            if (state == null) {
                state = PlantStateEntity(
                    id = 1,
                    points = 0,
                    growthLevel = 0f
                )
                plantDao.saveState(state)
            }

            plantPoints.postValue(state.points)
            plantGrowth.postValue(state.growthLevel)
        }
    }

    fun addWater() {
        viewModelScope.launch(Dispatchers.IO) {

            val state = plantDao.getState() ?: return@launch

            // ❗ Перевірка: чи є бали для поливу
            if (state.points <= 0) {
                // Немає балів — виходимо
                return@launch
            }

            // ❗ Зменшуємо бали
            val newPoints = state.points - 1

            // Збільшуємо ріст рослини
            val newGrowth = (state.growthLevel + 0.1f).coerceAtMost(5f)

            // Оновлений стан
            val updated = state.copy(
                points = newPoints,
                growthLevel = newGrowth
            )

            // Зберігаємо в базу
            plantDao.saveState(updated)

            // Оновлюємо LiveData
            plantPoints.postValue(updated.points)
            plantGrowth.postValue(updated.growthLevel)
        }
    }


    // ---------------- STATISTICS SYSTEM -------------------

    var habitsWithTaskLogs: List<HabitWithTaskLogs> = ArrayList()
    var lineChartColumns = 7
    private var selectedDate = DateTime.now()

    private val loading = MutableLiveData<Boolean>()
    private val completedTasksChartData = MutableLiveData<List<ChartDataModel>>()
    private val scheduledTasksChartData = MutableLiveData<List<ChartDataModel>>()

    // -------- ПУБЛІЧНІ GETTERS ДЛЯ ФРАГМЕНТА --------

    fun getLoadingLiveData(): LiveData<Boolean> = loading
    fun getHabitsWithTaskLogs(): LiveData<List<HabitWithTaskLogs>> =
        databaseManager.habitRepository.habitsWithTaskLogs

    fun getCompletedTasksChartData(): LiveData<List<ChartDataModel>> =
        completedTasksChartData

    fun getScheduledTasksChartData(): LiveData<List<ChartDataModel>> =
        scheduledTasksChartData

    fun getSelectedDate(): DateTime = selectedDate

    // ---------------- ACTIONS ----------------------

    fun setSelectedDate(date: DateTime) {
        selectedDate = date
        generateLineChartData()
    }

    fun setColumns(columns: Int) {
        lineChartColumns = columns
        generateLineChartData()
    }

    fun generateData() {
        loading.value = true
        viewModelScope.launch(Dispatchers.IO) {

            generateLineChartData()
            generateScheduledTasksChartData()

            loading.postValue(false)
        }
    }

    // ---------------- INTERNAL BUILDERS --------------------

    private fun generateLineChartData() {
        val fromDate = selectedDate.minusDays(lineChartColumns)

        completedTasksChartData.postValue(
            TaskUtil.getAmountOfDoneTasksForDateRange(
                getApplication(),
                habitsWithTaskLogs,
                fromDate,
                selectedDate
            ).reversed()
        )
    }

    private fun generateScheduledTasksChartData() {
        scheduledTasksChartData.postValue(
            TaskUtil.getAmountOfScheduledTasksPerWeekDay(
                getApplication(),
                habitsWithTaskLogs
            )
        )
    }
}
