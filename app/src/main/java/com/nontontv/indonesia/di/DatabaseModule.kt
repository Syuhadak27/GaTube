package com.nontontv.indonesia.di

import android.content.Context
import androidx.room.Room
import com.nontontv.indonesia.data.local.AppDatabase
import com.nontontv.indonesia.data.local.FavoriteDao
import com.nontontv.indonesia.data.local.HistoryDao
import com.nontontv.indonesia.data.local.SearchHistoryDao
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
    fun appDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "gatube.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun historyDao(db: AppDatabase): HistoryDao = db.historyDao()

    @Provides
    fun favoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun searchHistoryDao(db: AppDatabase): SearchHistoryDao = db.searchHistoryDao()
}
