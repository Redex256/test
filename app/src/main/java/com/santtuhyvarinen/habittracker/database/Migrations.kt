package com.santtuhyvarinen.habittracker.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `PlantStateEntity` (" +
                    "`id` INTEGER NOT NULL, " +
                    "`points` INTEGER NOT NULL, " +
                    "`growthLevel` REAL NOT NULL, " +
                    "PRIMARY KEY(`id`))"
        )

        database.execSQL(
            "INSERT OR REPLACE INTO PlantStateEntity (id, points, growthLevel) VALUES (0, 0, 0)"
        )
    }
}
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Таблиця повинна існувати після версії 2
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS PlantStateEntity (
                id INTEGER NOT NULL PRIMARY KEY,
                points INTEGER NOT NULL,
                growthLevel REAL NOT NULL
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            INSERT OR IGNORE INTO PlantStateEntity (id, points, growthLevel)
            VALUES (0, 0, 0.0)
            """.trimIndent()
        )
    }
}

