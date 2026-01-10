package com.heptad.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.heptad.app.data.database.dao.DailyCompletionDao
import com.heptad.app.data.database.dao.GameStateDao
import com.heptad.app.data.database.dao.PuzzleDao
import com.heptad.app.data.database.entity.DailyCompletionEntity
import com.heptad.app.data.database.entity.GameStateEntity
import com.heptad.app.data.database.entity.PuzzleEntity

/**
 * Room database for Heptad app
 */
@Database(
    entities = [
        PuzzleEntity::class,
        GameStateEntity::class,
        DailyCompletionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HeptadDatabase : RoomDatabase() {

    abstract fun puzzleDao(): PuzzleDao

    abstract fun gameStateDao(): GameStateDao

    abstract fun dailyCompletionDao(): DailyCompletionDao

    companion object {
        const val DATABASE_NAME = "heptad_database"

        /**
         * Migration from version 1 to 2: Add daily_completions table
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_completions (
                        puzzleId TEXT PRIMARY KEY NOT NULL,
                        puzzleNumber INTEGER NOT NULL,
                        completedDate TEXT NOT NULL,
                        completedAt INTEGER NOT NULL,
                        wordsFound INTEGER NOT NULL,
                        totalWords INTEGER NOT NULL,
                        scoreAchieved INTEGER NOT NULL,
                        maxScore INTEGER NOT NULL,
                        rankAchieved TEXT NOT NULL,
                        pangramsFound INTEGER NOT NULL,
                        totalPangrams INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_daily_completions_completedDate ON daily_completions(completedDate)"
                )

                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_daily_completions_puzzleNumber ON daily_completions(puzzleNumber)"
                )
            }
        }
    }
}
