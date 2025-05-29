package com.example.moviechecklist.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE isWatched = 1")
    fun getWatchedMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isInWishlist = 1")
    fun getWishlistMovies(): Flow<List<MovieEntity>>

    @Update
    suspend fun updateMovie(movie: MovieEntity)
}