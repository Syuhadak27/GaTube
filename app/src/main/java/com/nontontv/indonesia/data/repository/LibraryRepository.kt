package com.nontontv.indonesia.data.repository

import com.nontontv.indonesia.data.local.FavoriteDao
import com.nontontv.indonesia.data.local.FavoriteEntity
import com.nontontv.indonesia.data.local.HistoryDao
import com.nontontv.indonesia.data.local.HistoryEntity
import com.nontontv.indonesia.data.local.SearchHistoryDao
import com.nontontv.indonesia.data.local.SearchHistoryEntity
import com.nontontv.indonesia.data.model.VideoItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Mengelola history, favorit, dan riwayat pencarian (semuanya lokal). */
@Singleton
class LibraryRepository @Inject constructor(
    private val historyDao: HistoryDao,
    private val favoriteDao: FavoriteDao,
    private val searchHistoryDao: SearchHistoryDao
) {

    val riwayat: Flow<List<HistoryEntity>> = historyDao.semua()
    val favorit: Flow<List<FavoriteEntity>> = favoriteDao.semua()
    val riwayatPencarian: Flow<List<SearchHistoryEntity>> = searchHistoryDao.semua()

    suspend fun simpanRiwayat(video: VideoItem) {
        historyDao.simpan(video.toHistoryEntity(System.currentTimeMillis()))
    }

    suspend fun hapusRiwayat(videoId: String) = historyDao.hapus(videoId)

    suspend fun bersihkanRiwayat() = historyDao.bersihkan()

    suspend fun tambahFavorit(video: VideoItem) {
        favoriteDao.simpan(video.toFavoriteEntity(System.currentTimeMillis()))
    }

    suspend fun hapusFavorit(videoId: String) = favoriteDao.hapus(videoId)

    suspend fun apakahFavorit(videoId: String): Boolean =
        favoriteDao.cari(videoId) != null

    suspend fun simpanPencarian(query: String) {
        if (query.isBlank()) return
        searchHistoryDao.simpan(SearchHistoryEntity(query.trim(), System.currentTimeMillis()))
    }

    suspend fun hapusPencarian(query: String) = searchHistoryDao.hapus(query)

    suspend fun bersihkanPencarian() = searchHistoryDao.bersihkan()

    private fun VideoItem.toHistoryEntity(waktu: Long) = HistoryEntity(
        videoId = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        durationSeconds = durationSeconds,
        viewCount = viewCount,
        uploaderName = uploaderName,
        uploaderUrl = uploaderUrl,
        uploaderAvatarUrl = uploaderAvatarUrl,
        uploadDate = uploadDate,
        watchedAt = waktu
    )

    private fun VideoItem.toFavoriteEntity(waktu: Long) = FavoriteEntity(
        videoId = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        durationSeconds = durationSeconds,
        viewCount = viewCount,
        uploaderName = uploaderName,
        uploaderUrl = uploaderUrl,
        uploaderAvatarUrl = uploaderAvatarUrl,
        uploadDate = uploadDate,
        savedAt = waktu
    )
}
