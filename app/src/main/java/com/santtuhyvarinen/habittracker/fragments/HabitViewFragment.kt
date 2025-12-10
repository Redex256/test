package com.santtuhyvarinen.habittracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.santtuhyvarinen.habittracker.R
import com.santtuhyvarinen.habittracker.activities.MainActivity
import com.santtuhyvarinen.habittracker.databinding.FragmentHabitViewBinding
import com.santtuhyvarinen.habittracker.databinding.LayoutStatBinding
import com.santtuhyvarinen.habittracker.models.HabitWithTaskLogs
import com.santtuhyvarinen.habittracker.utils.CalendarUtil
import com.santtuhyvarinen.habittracker.utils.HabitInfoUtil
import com.santtuhyvarinen.habittracker.utils.StatisticsUtil
import com.santtuhyvarinen.habittracker.viewmodels.HabitViewModel

class HabitViewFragment : Fragment() {

    private var _binding: FragmentHabitViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var habitViewModel: HabitViewModel
    private val args: HabitViewFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentHabitViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // якщо id неправильний → назад
        if (args.habitId < 0) {
            findNavController().navigateUp()
            return
        }

        updateProgress(false)

        habitViewModel = ViewModelProvider(this)[HabitViewModel::class.java]
        habitViewModel.initialize(args.habitId)

        observeHabit()
        observeExit()
        setupToolbarButtons()
        setupUIActions()

        // назви стат блоків
        setStatHeader(binding.statCreated, getString(R.string.created))
        setStatHeader(binding.statTotalSuccesses, getString(R.string.total_success))
        setStatHeader(binding.statHighestScore, getString(R.string.highest_score))
    }

    private fun observeHabit() {
        habitViewModel.getHabitWithTaskLogsLiveData()
            .observe(viewLifecycleOwner) { habit ->
                if (habit != null) {
                    updateHabitValues(habit)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.error_load_habit), Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
            }
    }

    private fun observeExit() {
        habitViewModel.getShouldExitView()
            .observe(viewLifecycleOwner) { exit ->
                if (exit) findNavController().navigateUp()
            }
    }

    private fun setupToolbarButtons() {
        val activity = (activity as MainActivity)

        activity.getEditButton().setOnClickListener {
            val action =
                HabitViewFragmentDirections.actionFromHabitViewFragmentToHabitFormFragment(args.habitId)
            findNavController().navigate(action)
        }

        activity.getDeleteButton().setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun setupUIActions() {
        binding.habitDisableSwitch.setOnClickListener {
            habitViewModel.setHabitEnabled(binding.habitDisableSwitch.isChecked)

            val toastStringId =
                if (binding.habitDisableSwitch.isChecked) R.string.tasks_enabled
                else R.string.tasks_disabled

            Toast.makeText(requireContext(), getString(toastStringId), Toast.LENGTH_SHORT).show()
        }

        binding.viewTaskLogsButton.setOnClickListener {
            navigateToTaskManagement()
        }
    }

    private fun updateProgress(showLayout: Boolean) {
        binding.progress.visibility = if (showLayout) View.GONE else View.VISIBLE
        binding.habitInfoCard.visibility = if (showLayout) View.VISIBLE else View.GONE
        binding.scrollView.visibility = if (showLayout) View.VISIBLE else View.GONE
        binding.viewTaskLogsButton.visibility = if (showLayout) View.VISIBLE else View.GONE
    }


    private fun updateHabitValues(habitWithTaskLogs: HabitWithTaskLogs) {
        val habit = habitWithTaskLogs.habit

        binding.habitNameText.text = habit.name

        // пріоритет
        val priorityText = HabitInfoUtil.getPriorityLevelText(requireContext(), habit.priority)
        binding.habitPriorityText.text =
            getString(R.string.habit_priority_header, priorityText)

        // повторення
        binding.habitRecurrenceText.text =
            habitViewModel.getRecurrenceText(requireContext(), habit)

        // значок
        habit.iconKey?.let {
            binding.habitIcon.setImageDrawable(habitViewModel.iconManager.getIconByKey(it))
        }

        // очки
        binding.scoreTextView.text = getString(R.string.score_text, habit.score)

        // перемикач
        binding.habitDisableSwitch.isChecked = !habit.disabled

        // статистика
        updateStatValue(binding.statCreated,
            CalendarUtil.getDateText(habit.creationDate, requireContext())
        )
        updateStatValue(binding.statTotalSuccesses,
            StatisticsUtil.getTotalSuccesses(habitWithTaskLogs).toString()
        )

        val highestScore = StatisticsUtil.getHighestScore(habitWithTaskLogs.taskLogs)
        updateStatValue(binding.statHighestScore, getString(R.string.score_text, highestScore))

        // timeline
        binding.habitTimelineView.setup(habitWithTaskLogs)

        updateProgress(true)
    }

    private fun setStatHeader(stat: LayoutStatBinding, headerText: String) {
        stat.statHeaderText.text = headerText
    }

    private fun updateStatValue(stat: LayoutStatBinding, value: String) {
        stat.statValueText.text = value
    }

    private fun showDeleteConfirmationDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_habit))
            .setMessage(getString(R.string.habit_delete_confirmation))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                habitViewModel.deleteHabit(requireContext())
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
    }

    private fun navigateToTaskManagement() {
        val direction =
            HabitViewFragmentDirections.actionFromHabitViewFragmentToTaskManagementFragment(args.habitId)
        findNavController().navigate(direction)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
