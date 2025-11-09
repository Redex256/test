package com.santtuhyvarinen.habittracker.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.santtuhyvarinen.habittracker.database.PlantStateEntity

@Dao
interface PlantStateDao {

    @Query("SELECT * FROM PlantStateEntity WHERE id = 1")
    fun getState(): PlantStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveState(state: PlantStateEntity)
}
