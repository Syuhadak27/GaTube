package com.nontontv.indonesia.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/** Basis data lokal GaTube (Room). */
@Database(
    entities = [
        HistoryEntity::class,
        FavoriteEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}
