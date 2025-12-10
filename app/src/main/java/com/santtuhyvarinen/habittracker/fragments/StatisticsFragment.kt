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

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: StatisticsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[StatisticsViewModel::class.java]

        setupObservers()
        setupButtons()
        setupHeaders()

        return binding.root
    }

    // ---------------------------------------------------------
    // OBSERVERS
    // ---------------------------------------------------------
    private fun setupObservers() {

        viewModel.getLoadingLiveData().observe(viewLifecycleOwner) { loading ->
            updateProgress(!loading)
        }

        viewModel.getHabitsWithTaskLogs().observe(viewLifecycleOwner) { habits ->
            viewModel.habitsWithTaskLogs = habits
            updateStats()
            viewModel.generateData()
        }

        viewModel.getCompletedTasksChartData().observe(viewLifecycleOwner) {
            updateLineChartView(it)
        }

        viewModel.getScheduledTasksChartData().observe(viewLifecycleOwner) {
            updateScheduledTasksChartView(it)
        }
    }

    // ---------------------------------------------------------
    // BUTTONS
    // ---------------------------------------------------------
    private fun setupButtons() {
        binding.selectColumnsLineChartViewButton.setOnClickListener { showColumnsMenu() }
        binding.selectDateLineChartViewButton.setOnClickListener { showDatePicker() }
    }

    // ---------------------------------------------------------
    // HEADERS
    // ---------------------------------------------------------
    private fun setupHeaders() {
        setHeader(binding.statHabits, getString(R.string.stat_habits))
        setHeader(binding.statTotalSuccesses, getString(R.string.total_success))
        setHeader(binding.statAverageSuccesses, getString(R.string.stat_average_task_success_per_day))
    }

    // ---------------------------------------------------------
    // UPDATE STATISTICS
    // ---------------------------------------------------------
    private fun updateStats() {
        val list = viewModel.habitsWithTaskLogs
        updateValue(binding.statHabits, list.size.toString())
        updateValue(binding.statTotalSuccesses, StatisticsUtil.getTotalSuccessesForHabits(list).toString())
        updateValue(binding.statAverageSuccesses, "%.1f".format(StatisticsUtil.getAverageTasksCompletedByDay(list)))
    }

    private fun setHeader(stat: LayoutStatBinding, text: String) {
        stat.statHeaderText.text = text
    }

    private fun updateValue(stat: LayoutStatBinding, value: String) {
        stat.statValueText.text = value
    }

    // ---------------------------------------------------------
    // LINE CHART
    // ---------------------------------------------------------
    private fun updateLineChartView(data: List<ChartDataModel>) {
        binding.completedTasksChartView.chartData = data
        binding.completedTasksChartView.columns = viewModel.lineChartColumns
        binding.completedTasksChartView.rows =
            if (data.isNotEmpty()) data.maxOf { it.value } + 2 else 0
    }

    // ---------------------------------------------------------
    // COLUMN CHART
    // ---------------------------------------------------------
    private fun updateScheduledTasksChartView(data: List<ChartDataModel>) {
        binding.scheduledTasksChartView.chartData = data
        binding.scheduledTasksChartView.rows =
            if (data.isNotEmpty()) data.maxOf { it.value } + 2 else 0
    }

    // ---------------------------------------------------------
    // DATE PICKER
    // ---------------------------------------------------------
    private fun showDatePicker() {
        val d = viewModel.getSelectedDate()

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                viewModel.setSelectedDate(
                    DateTime.now().withYear(year).withMonthOfYear(month + 1).withDayOfMonth(day)
                )
            },
            d.year,
            d.monthOfYear - 1,
            d.dayOfMonth
        ).show()
    }

    // ---------------------------------------------------------
    // MENU FOR COLUMNS
    // ---------------------------------------------------------
    private fun showColumnsMenu() {
        val menu = PopupMenu(requireContext(), binding.selectColumnsLineChartViewButton)
        menu.menuInflater.inflate(R.menu.menu_select_columns, menu.menu)

        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.columnsWeekView -> viewModel.setColumns(7)
                R.id.columnsTwoWeeksView -> viewModel.setColumns(14)
                R.id.columnsMonthView -> viewModel.setColumns(30)
            }
            true
        }

        menu.show()
    }

    // ---------------------------------------------------------
    // PROGRESS BAR
    // ---------------------------------------------------------
    private fun updateProgress(visible: Boolean) {
        binding.progress.visibility = if (visible) View.GONE else View.VISIBLE
        binding.scrollView.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
