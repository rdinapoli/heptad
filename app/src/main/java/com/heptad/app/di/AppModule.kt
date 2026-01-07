package com.heptad.app.di

import android.content.Context
import androidx.room.Room
import com.heptad.app.data.database.HeptadDatabase
import com.heptad.app.data.database.dao.GameStateDao
import com.heptad.app.data.database.dao.PuzzleDao
import com.heptad.app.data.repository.DictionaryRepository
import com.heptad.app.domain.PuzzleGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing app-wide dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): HeptadDatabase {
        return Room.databaseBuilder(
            context,
            HeptadDatabase::class.java,
            HeptadDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun providePuzzleDao(database: HeptadDatabase): PuzzleDao {
        return database.puzzleDao()
    }

    @Provides
    @Singleton
    fun provideGameStateDao(database: HeptadDatabase): GameStateDao {
        return database.gameStateDao()
    }

    @Provides
    @Singleton
    fun provideDictionaryRepository(
        @ApplicationContext context: Context
    ): DictionaryRepository {
        return DictionaryRepository(context)
    }

    @Provides
    @Singleton
    fun providePuzzleGenerator(
        dictionaryRepository: DictionaryRepository
    ): PuzzleGenerator {
        return PuzzleGenerator(dictionaryRepository)
    }
}
