package com.yourpackage.moviechecklist.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Delete
    suspend fun deleteMovie(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE id = :id")
    fun getMovieById(id: Int): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE status = :status ORDER BY title ASC")
    fun getMoviesByStatus(status: MovieStatus): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%'") // Simple local title search
    fun searchLocalMovies(query: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies")
    fun getAllMovies(): Flow<List<MovieEntity>> // For simple local filtering

}