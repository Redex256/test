package com.santtuhyvarinen.habittracker.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.santtuhyvarinen.habittracker.R
import com.santtuhyvarinen.habittracker.databinding.FragmentStatisticsBinding
import com.santtuhyvarinen.habittracker.databinding.LayoutStatBinding
import com.santtuhyvarinen.habittracker.models.ChartDataModel
import com.santtuhyvarinen.habittracker.utils.StatisticsUtil
import com.santtuhyvarinen.habittracker.viewmodels.StatisticsViewModel
import org.joda.time.DateTime

class StatisticsFragment : Fragment() {

    private lateinit var statisticsViewModel: StatisticsViewModel

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)

        // ViewModel
        statisticsViewModel = ViewModelProvider(this)[StatisticsViewModel::class.java]

        // Loading observer
        statisticsViewModel.getLoadingLiveData().observe(viewLifecycleOwner) { loading ->
            updateProgress(!loading)
        }

        // Habits observer
        statisticsViewModel.getHabitsWithTaskLogs().observe(viewLifecycleOwner) { list ->
            statisticsViewModel.habitsWithTaskLogs = list
            updateStats()
            statisticsViewModel.generateData()
        }

        // Chart observers
        statisticsViewModel.getCompletedTasksChartData().observe(viewLifecycleOwner) {
            updateLineChartView(it)
        }

        statisticsViewModel.getScheduledTasksChartData().observe(viewLifecycleOwner) {
            updateScheduledTasksChartView(it)
        }

        // -----------------------------
        // PLANT OBSERVERS
        // -----------------------------
        statisticsViewModel.plantPoints.observe(viewLifecycleOwner) {
            binding.plantPointsText.text = "Бали: $it"
        }

        statisticsViewModel.plantGrowth.observe(viewLifecycleOwner) {
            updatePlantImage(it)
        }

        binding.waterPlantButton.setOnClickListener {
            statisticsViewModel.addWater()   // <-- FIXED
        }
        // -----------------------------

        // Buttons
        binding.selectColumnsLineChartViewButton.setOnClickListener { showColumnsMenu() }
        binding.selectDateLineChartViewButton.setOnClickListener { showDatePickerDialog() }

        // Stat headers
        setStatHeader(binding.statHabits, getString(R.string.stat_habits))
        setStatHeader(binding.statTotalSuccesses, getString(R.string.total_success))
        setStatHeader(binding.statAverageSuccesses, getString(R.string.stat_average_task_success_per_day))

        return binding.root
    }

    // ---------------------------------------
    // CHART UPDATE FUNCTIONS
    // ---------------------------------------

    private fun updateLineChartView(data: List<ChartDataModel>) {
        binding.completedTasksChartView.chartData = data
        binding.completedTasksChartView.columns = statisticsViewModel.lineChartColumns
        binding.completedTasksChartView.rows =
            if (data.isNotEmpty()) (data.maxOf { it.value }.coerceAtLeast(5)) + 1 else 0
    }

    private fun updateScheduledTasksChartView(data: List<ChartDataModel>) {
        binding.scheduledTasksChartView.chartData = data
        binding.scheduledTasksChartView.rows =
            if (data.isNotEmpty()) (data.maxOf { it.value }.coerceAtLeast(5)) + 1 else 0
    }

    // ---------------------------------------
    // DATE PICKER
    // ---------------------------------------

    private fun showDatePickerDialog() {
        val selectedDate = statisticsViewModel.getSelectedDate()

        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val date = DateTime.now()
                    .withYear(year)
                    .withMonthOfYear(month + 1)
                    .withDayOfMonth(day)

                statisticsViewModel.setSelectedDate(date)
            },
            selectedDate.year,
            selectedDate.monthOfYear - 1,
            selectedDate.dayOfMonth
        )

        dialog.show()
    }

    // ---------------------------------------
    // COLUMNS MENU
    // ---------------------------------------

    private fun showColumnsMenu() {
        val popupMenu = PopupMenu(activity, binding.selectColumnsLineChartViewButton)
        popupMenu.menuInflater.inflate(R.menu.menu_select_columns, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.columnsWeekView -> statisticsViewModel.setColumns(7)
                R.id.columnsTwoWeeksView -> statisticsViewModel.setColumns(14)
                R.id.columnsMonthView -> statisticsViewModel.setColumns(30)
            }
            true
        }

        popupMenu.show()
    }

    // ---------------------------------------
    // STATS UPDATE
    // ---------------------------------------

    private fun updateStats() {
        val habits = statisticsViewModel.habitsWithTaskLogs
        updateStatValue(binding.statHabits, habits.size.toString())
        updateStatValue(binding.statTotalSuccesses, StatisticsUtil.getTotalSuccessesForHabits(habits).toString())
        updateStatValue(binding.statAverageSuccesses, "%.1f".format(StatisticsUtil.getAverageTasksCompletedByDay(habits)))
    }

    private fun setStatHeader(stat: LayoutStatBinding, headerText: String) {
        stat.statHeaderText.text = headerText
    }

    private fun updateStatValue(stat: LayoutStatBinding, value: String) {
        stat.statValueText.text = value
    }

    // ---------------------------------------
    // UPDATE PLANT IMAGE
    // ---------------------------------------

    private fun updatePlantImage(level: Float) {
        val img = when {
            level < 0.2f -> R.drawable.plant_stage_0
            level < 0.4f -> R.drawable.plant_stage_1
            level < 0.6f -> R.drawable.plant_stage_2
            level < 0.8f -> R.drawable.plant_stage_3
            else -> R.drawable.plant_stage_4
        }
        binding.plantImageView.setImageResource(img)
    }

    // ---------------------------------------
    // PROGRESS HANDLING
    // ---------------------------------------

    private fun updateProgress(showLayout: Boolean) {
        binding.progress.visibility = if (showLayout) View.GONE else View.VISIBLE
        binding.scrollView.visibility = if (showLayout) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
