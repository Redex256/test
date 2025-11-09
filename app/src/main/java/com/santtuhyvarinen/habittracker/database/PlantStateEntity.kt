package com.santtuhyvarinen.habittracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PlantStateEntity")
data class PlantStateEntity(
    @PrimaryKey val id: Int = 0,
    val points: Int = 0,
    val growthLevel: Float = 0f
)
