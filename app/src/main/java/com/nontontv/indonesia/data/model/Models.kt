package com.nontontv.indonesia.data.model

/**
 * Model-domain yang dipakai di seluruh lapisan UI.
 * Semua teks sudah dalam bahasa Indonesia pada lapisan UI.
 */

/** Representasi ringkas sebuah video untuk daftar. */
data class VideoItem(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val durationSeconds: Long,
    val viewCount: Long,
    val uploaderName: String,
    val uploaderUrl: String,
    val uploaderAvatarUrl: String,
    val uploadDate: String,
    val isLive: Boolean = false,
    val isShort: Boolean = false
)

/** Pilihan kualitas video dari extractor. */
data class StreamOption(
    val url: String,
    val quality: String,
    val height: Int,
    val fps: Int,
    val isVideoOnly: Boolean,
    val mimeType: String
)

/** Pilihan audio dari extractor (dipakai untuk menggabung video-only 4K). */
data class AudioOption(
    val url: String,
    val bitrate: Int,
    val mimeType: String
)

/** Detail lengkap video. */
data class VideoDetail(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val durationSeconds: Long,
    val viewCount: Long,
    val likeCount: Long,
    val uploaderName: String,
    val uploaderUrl: String,
    val uploaderAvatarUrl: String,
    val uploaderSubscriberCount: Long,
    val uploadDate: String,
    val videoStreams: List<StreamOption>,
    val audioStreams: List<AudioOption>,
    val related: List<VideoItem>
)

/** Komentar video. */
data class CommentItem(
    val id: String,
    val author: String,
    val authorAvatarUrl: String,
    val text: String,
    val likeCount: Int,
    val publishedTime: String,
    val isHearted: Boolean = false,
    val isPinned: Boolean = false,
    val replyCount: Int = 0
)

/** Detail kanal. */
data class ChannelDetail(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val bannerUrl: String,
    val subscriberCount: Long,
    val description: String,
    val videoCount: Long,
    val videos: List<VideoItem>
)

/** Detail playlist YouTube. */
data class PlaylistDetail(
    val id: String,
    val name: String,
    val thumbnailUrl: String,
    val uploaderName: String,
    val uploaderUrl: String,
    val videoCount: Long,
    val videos: List<VideoItem>
)

/** Sesi akun hasil pairing. */
data class Account(
    val token: String,
    val deviceName: String,
    val linkedAt: Long
)

/** Kategori/kiosk YouTube (Trending, Musik, Game, dll). */
data class Kategori(
    val id: String,
    val nama: String
)

/** Informasi pembaruan yang tersedia dari GitHub Release. */
data class UpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val changelog: String,
    val apkUrl: String
)

/**
 * Kode pairing lokal. Karena tidak memakai server, kode hanya disimpan di
 * perangkat ini (DataStore) dan cocok antar-entri yang ada di perangkat yang sama.
 */
data class KodePairingLokal(
    val kode: String,
    val status: String,
    val token: String?,
    val kedaluwarsa: Long
)
