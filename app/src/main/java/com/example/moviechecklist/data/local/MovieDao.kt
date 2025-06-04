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

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): MovieEntity? // Added for direct access if needed

    @Query("SELECT * FROM movies WHERE isWatched = 1 ORDER BY title ASC") // Added ORDER BY
    fun getWatchedMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isInWishlist = 1 ORDER BY title ASC") // Added ORDER BY
    fun getWishlistMovies(): Flow<List<MovieEntity>>

    @Update
    suspend fun updateMovie(movie: MovieEntity) // This is fine if we fetch, modify, then update
}