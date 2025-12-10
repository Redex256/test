package com.santtuhyvarinen.habittracker.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class PlantGrowthViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("plant_prefs", 0)

    val waterLeft = MutableLiveData<Int>()
    val plantLevel = MutableLiveData<Float>()

    init {
        waterLeft.value = prefs.getInt("water_left", 3)
        plantLevel.value = prefs.getFloat("plant_level", 0f)
    }

    fun waterPlant() {
        val currentWater = waterLeft.value ?: 0
        val currentLevel = plantLevel.value ?: 0f

        // Якщо вода закінчилась — нічого не робимо
        if (currentWater <= 0) return

        // ↓↓↓ ОБОВʼЯЗКОВО ПЕРЕПИСУЄМО LiveData + prefs ↓↓↓

        val newWater = currentWater - 1
        waterLeft.value = newWater
        prefs.edit().putInt("water_left", newWater).apply()

        val newLevel = (currentLevel + 0.1f).coerceAtMost(1f)
        plantLevel.value = newLevel
        prefs.edit().putFloat("plant_level", newLevel).apply()
    }
}
