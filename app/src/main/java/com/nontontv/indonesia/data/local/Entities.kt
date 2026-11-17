package com.nontontv.indonesia.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Riwayat tontonan tersimpan di perangkat. */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val durationSeconds: Long,
    val viewCount: Long,
    val uploaderName: String,
    val uploaderUrl: String,
    val uploaderAvatarUrl: String,
    val uploadDate: String,
    val watchedAt: Long
)

/** Video favorit tersimpan di perangkat. */
@Entity(tableName = "favorite")
data class FavoriteEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val durationSeconds: Long,
    val viewCount: Long,
    val uploaderName: String,
    val uploaderUrl: String,
    val uploaderAvatarUrl: String,
    val uploadDate: String,
    val savedAt: Long
)

/** Riwayat kata kunci pencarian. */
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long
)
