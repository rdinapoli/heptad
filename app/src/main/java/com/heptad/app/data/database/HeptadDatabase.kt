package com.heptad.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.heptad.app.data.database.dao.GameStateDao
import com.heptad.app.data.database.dao.PuzzleDao
import com.heptad.app.data.database.entity.GameStateEntity
import com.heptad.app.data.database.entity.PuzzleEntity

/**
 * Room database for Heptad app
 */
@Database(
    entities = [
        PuzzleEntity::class,
        GameStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HeptadDatabase : RoomDatabase() {

    abstract fun puzzleDao(): PuzzleDao

    abstract fun gameStateDao(): GameStateDao

    companion object {
        const val DATABASE_NAME = "heptad_database"
    }
}
