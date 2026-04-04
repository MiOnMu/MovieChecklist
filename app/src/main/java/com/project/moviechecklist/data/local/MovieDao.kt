package com.project.moviechecklist.data.local

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

    @Query("SELECT * FROM movies WHERE id = :id AND userId = :userId")
    fun getMovieById(id: Int, userId: String): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE status = :status AND userId = :userId ORDER BY title ASC")
    fun getMoviesByStatus(status: MovieStatus, userId: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' AND userId = :userId")
    fun searchLocalMovies(query: String, userId: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE userId = :userId")
    fun getAllMovies(userId: String): Flow<List<MovieEntity>>
}
