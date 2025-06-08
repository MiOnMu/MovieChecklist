package com.yourpackage.moviechecklist.di

import android.content.Context
import androidx.room.Room
import com.yourpackage.moviechecklist.data.local.AppDatabase
import com.yourpackage.moviechecklist.data.local.MovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "movie_checklist_db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build() // Use migrations for production
    }

    @Provides
    @Singleton
    fun provideMovieDao(appDatabase: AppDatabase): MovieDao {
        return appDatabase.movieDao()
    }
}