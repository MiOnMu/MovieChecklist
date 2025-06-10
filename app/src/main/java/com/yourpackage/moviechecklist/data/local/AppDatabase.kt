package com.yourpackage.moviechecklist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters as RoomTypeConverters

@Database(entities = [MovieEntity::class], version = 2, exportSchema = false)
@RoomTypeConverters(com.yourpackage.moviechecklist.data.local.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}