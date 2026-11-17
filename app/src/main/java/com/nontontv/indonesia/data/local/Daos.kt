package com.nontontv.indonesia.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY watchedAt DESC")
    fun semua(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE videoId = :videoId LIMIT 1")
    suspend fun cari(videoId: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpan(entity: HistoryEntity)

    @Query("DELETE FROM history WHERE videoId = :videoId")
    suspend fun hapus(videoId: String)

    @Query("DELETE FROM history")
    suspend fun bersihkan()
}

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorite ORDER BY savedAt DESC")
    fun semua(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorite WHERE videoId = :videoId LIMIT 1")
    suspend fun cari(videoId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpan(entity: FavoriteEntity)

    @Query("DELETE FROM favorite WHERE videoId = :videoId")
    suspend fun hapus(videoId: String)

    @Query("DELETE FROM favorite")
    suspend fun bersihkan()
}

@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 20")
    fun semua(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpan(entity: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun hapus(query: String)

    @Query("DELETE FROM search_history")
    suspend fun bersihkan()
}
