package com.santtuhyvarinen.habittracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.santtuhyvarinen.habittracker.R
import com.santtuhyvarinen.habittracker.databinding.FragmentPlantGrowthBinding
import com.santtuhyvarinen.habittracker.viewmodels.PlantGrowthViewModel

class PlantGrowthFragment : Fragment() {

    private lateinit var binding: FragmentPlantGrowthBinding
    private lateinit var viewModel: PlantGrowthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlantGrowthBinding.inflate(inflater, container, false)

        // ✔ Використовуємо спільний ViewModel для activity
        viewModel = ViewModelProvider(requireActivity())[PlantGrowthViewModel::class.java]

        // 🔵 Оновлення тексту "Можна полити"
        viewModel.waterLeft.observe(viewLifecycleOwner) { water ->
            binding.waterLeftText.text = "Можна полити: $water"
        }

        // 🔵 Оновлення росту та картинки
        viewModel.plantLevel.observe(viewLifecycleOwner) { level ->
            binding.growthText.text = "Ріст: ${(level * 100).toInt()}%"

            binding.plantImage.setImageResource(
                when {
                    level < 0.2f -> R.drawable.plant_stage_0
                    level < 0.4f -> R.drawable.plant_stage_1
                    level < 0.6f -> R.drawable.plant_stage_2
                    level < 0.8f -> R.drawable.plant_stage_3
                    else -> R.drawable.plant_stage_4
                }
            )
        }

        // 🔵 Кнопка поливу
        binding.waterPlantButton.setOnClickListener {
            viewModel.waterPlant()
        }

        return binding.root
    }
}

