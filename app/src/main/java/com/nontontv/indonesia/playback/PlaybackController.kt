package com.nontontv.indonesia.playback

import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.DefaultHttpDataSource
import com.nontontv.indonesia.data.model.AudioOption
import com.nontontv.indonesia.data.model.StreamOption
import com.nontontv.indonesia.data.model.VideoItem
import com.nontontv.indonesia.data.repository.LibraryRepository
import com.nontontv.indonesia.service.PlaybackService
import com.nontontv.indonesia.util.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import javax.inject.Inject
import javax.inject.Singleton

/** Video yang siap diputar beserta pilihan stream-nya. */
data class PlayableVideo(
    val item: VideoItem,
    val videoStreams: List<StreamOption>,
    val audioStreams: List<AudioOption>
)

/**
 * Pemutar bersama (ExoPlayer) yang dipakai layar pemutar dan service latar.
 * Karena satu proses, layar dan notifikasi media memakai player yang sama.
 */
@Singleton
class PlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val libraryRepository: LibraryRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _sedangMemutar = MutableStateFlow(false)
    val sedangMemutar: StateFlow<Boolean> = _sedangMemutar.asStateFlow()

    private val _posisi = MutableStateFlow(0L)
    val posisi: StateFlow<Long> = _posisi.asStateFlow()

    private val _durasi = MutableStateFlow(0L)
    val durasi: StateFlow<Long> = _durasi.asStateFlow()

    private val _kecepatan = MutableStateFlow(1.0f)
    val kecepatan: StateFlow<Float> = _kecepatan.asStateFlow()

    private val _kualitasTerpilih = MutableStateFlow(Config.KUALITAS_BAWAAN)
    val kualitasTerpilih: StateFlow<String> = _kualitasTerpilih.asStateFlow()

    private val _antrian = MutableStateFlow<List<PlayableVideo>>(emptyList())
    val antrian: StateFlow<List<PlayableVideo>> = _antrian.asStateFlow()

    private val _indeks = MutableStateFlow(0)
    val indeks: StateFlow<Int> = _indeks.asStateFlow()

    private val _videoSaatIni = MutableStateFlow<PlayableVideo?>(null)
    val videoSaatIni: StateFlow<PlayableVideo?> = _videoSaatIni.asStateFlow()

    private val _memuat = MutableStateFlow(false)
    val memuat: StateFlow<Boolean> = _memuat.asStateFlow()

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build(),
            /* handleAudioFocus = */ true
        )
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_NETWORK)
        .build()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _sedangMemutar.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _memuat.value = playbackState == Player.STATE_BUFFERING
            if (playbackState == Player.STATE_ENDED) {
                if (lanjutAda()) {
                    putarDiIndeks(_indeks.value + 1, _kualitasTerpilih.value, 0L)
                } else {
                    _sedangMemutar.value = false
                }
            }
        }
    }

    init {
        player.addListener(listener)
        player.prepare()
        scope.launch {
            while (isActive) {
                _posisi.value = player.currentPosition.coerceAtLeast(0L)
                _durasi.value = player.duration.coerceAtLeast(0L)
                delay(500L)
            }
        }
    }

    /** Memutar daftar video mulai dari indeks tertentu. */
    fun putar(
        daftar: List<PlayableVideo>,
        indeksAwal: Int = 0,
        kualitas: String = _kualitasTerpilih.value,
        posisiAwal: Long = 0L
    ) {
        if (daftar.isEmpty()) return
        _antrian.value = daftar
        putarDiIndeks(indeksAwal, kualitas, posisiAwal)
    }

    /** Memutar video pada indeks antrian. */
    fun putarDiIndeks(indeksBaru: Int, kualitas: String, posisiAwal: Long = 0L) {
        val daftar = _antrian.value
        if (indeksBaru < 0 || indeksBaru >= daftar.size) return
        val video = daftar[indeksBaru]

        val streamVideo = pilihStream(video, kualitas)
        if (streamVideo == null) {
            _memuat.value = false
            return
        }
        val streamAudio = if (streamVideo.isVideoOnly) {
            video.audioStreams.filter { it.url.isNotBlank() }.maxByOrNull { it.bitrate }
        } else {
            null
        }

        _indeks.value = indeksBaru
        _videoSaatIni.value = video
        _kualitasTerpilih.value = streamVideo.quality

        val httpFactory = DefaultHttpDataSource.Factory()
        val videoSource = ProgressiveMediaSource.Factory(httpFactory)
            .createMediaSource(MediaItem.Builder().setUri(streamVideo.url).build())

        val sumber: MediaSource = if (streamAudio != null) {
            val audioSource = ProgressiveMediaSource.Factory(httpFactory)
                .createMediaSource(MediaItem.Builder().setUri(streamAudio.url).build())
            MergingMediaSource(videoSource, audioSource)
        } else {
            videoSource
        }

        player.setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(video.item.title)
                .setArtist(video.item.uploaderName)
                .setArtworkUri(video.item.thumbnailUrl.toUriOrNull())
                .build()
        )
        player.setMediaSource(sumber, posisiAwal)
        player.prepare()
        player.playWhenReady = true
        player.setPlaybackSpeed(_kecepatan.value)

        scope.launch { libraryRepository.simpanRiwayat(video.item) }
    }

    /** Mengganti kualitas lalu memutar ulang dari posisi sekarang. */
    fun pilihKualitas(kualitas: String) {
        _kualitasTerpilih.value = kualitas
        putarDiIndeks(_indeks.value, kualitas, _posisi.value)
    }

    /** Mengatur kecepatan putar 0.25x - 2x. */
    fun aturKecepatan(kecepatan: Float) {
        val nilai = kecepatan.coerceIn(0.25f, 2.0f)
        _kecepatan.value = nilai
        player.setPlaybackSpeed(nilai)
    }

    fun jedaLanjut() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun lompatKe(persen: Float) {
        val durasi = _durasi.value
        if (durasi > 0) {
            player.seekTo((durasi * persen.coerceIn(0f, 1f)).toLong())
        }
    }

    fun lanjut() {
        if (lanjutAda()) putarDiIndeks(_indeks.value + 1, _kualitasTerpilih.value, 0L)
    }

    fun sebelumnya() {
        if (_posisi.value > 3000L) {
            player.seekTo(0L)
        } else if (_indeks.value > 0) {
            putarDiIndeks(_indeks.value - 1, _kualitasTerpilih.value, 0L)
        }
    }

    /** Menghentikan pemutaran dan menutup notifikasi media. */
    fun tutup() {
        player.stop()
        player.clearMediaItems()
        _antrian.value = emptyList()
        _videoSaatIni.value = null
        _sedangMemutar.value = false
        _posisi.value = 0L
        _durasi.value = 0L
        context.stopService(Intent(context, PlaybackService::class.java))
    }

    private fun lanjutAda(): Boolean = _indeks.value + 1 < _antrian.value.size

    /**
     * Memilih stream video terdekat dengan kualitas yang diinginkan.
     * Stream gabungan (video+audio) diprioritaskan; stream video-only dipakai
     * untuk kualitas tinggi lalu digabung dengan stream audio.
     */
    private fun pilihStream(video: PlayableVideo, kualitas: String): StreamOption? {
        val target = parseTinggi(kualitas)
        val gabungan = video.videoStreams.filter { !it.isVideoOnly && it.url.isNotBlank() }
        val videoOnly = video.videoStreams.filter { it.isVideoOnly && it.url.isNotBlank() }
        return gabungan.minByOrNull { abs(it.height - target) }
            ?: videoOnly.minByOrNull { abs(it.height - target) }
    }

    /** "1080p" -> 1080, "4K" -> 2160, "720p60" -> 720. */
    private fun parseTinggi(kualitas: String): Int {
        val angka = Regex("\\d+").find(kualitas)?.value?.toIntOrNull() ?: 720
        return if (kualitas.contains("4K", ignoreCase = true) && angka < 1000) 2160 else angka
    }

    private fun String.toUriOrNull(): Uri? = runCatching { Uri.parse(this) }.getOrNull()
}
