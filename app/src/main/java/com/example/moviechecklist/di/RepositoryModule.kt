package com.example.moviechecklist.di

import com.example.moviechecklist.api.TMDBService
import com.example.moviechecklist.data.local.MovieDao
import com.example.moviechecklist.repository.MovieRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMovieRepository(tmdbService: TMDBService, movieDao: MovieDao): MovieRepository {
        return MovieRepository(tmdbService, movieDao)
    }
}