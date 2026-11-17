package com.nontontv.indonesia.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nontontv.indonesia.data.local.SettingsRepository
import com.nontontv.indonesia.data.model.VideoDetail
import com.nontontv.indonesia.data.model.VideoItem
import com.nontontv.indonesia.data.remote.youtube.YouTubeRepository
import com.nontontv.indonesia.playback.PlaybackController
import com.nontontv.indonesia.playback.PlayableVideo
import com.nontontv.indonesia.ui.home.KeadaanMuat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel layar pemutar. Pemutaran sebenarnya dijalankan PlaybackController
 * (ExoPlayer + MediaSession) supaya background play dan notifikasi bekerja.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackController: PlaybackController,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _detail = MutableStateFlow<VideoDetail?>(null)
    val detail: StateFlow<VideoDetail?> = _detail.asStateFlow()

    private val _keadaan = MutableStateFlow<KeadaanMuat>(KeadaanMuat.Memuat)
    val keadaan: StateFlow<KeadaanMuat> = _keadaan.asStateFlow()

    private val _tampilKualitas = MutableStateFlow(false)
    val tampilKualitas: StateFlow<Boolean> = _tampilKualitas.asStateFlow()

    private val _tampilKecepatan = MutableStateFlow(false)
    val tampilKecepatan: StateFlow<Boolean> = _tampilKecepatan.asStateFlow()

    // Status pemutar yang diteruskan dari PlaybackController.
    val player get() = playbackController.player
    val sedangMemutar = playbackController.sedangMemutar
    val posisi = playbackController.posisi
    val durasi = playbackController.durasi
    val kecepatan = playbackController.kecepatan
    val kualitasTerpilih = playbackController.kualitasTerpilih
    val videoSaatIni = playbackController.videoSaatIni
    val memuat = playbackController.memuat

    private var kualitasSaatIni: String? = null

    init {
        viewModelScope.launch {
            val kualitas = settingsRepository.kualitasDefault.first()
            val kecepatan = settingsRepository.kecepatanDefault.first()
            kualitasSaatIni = kualitas
            playbackController.aturKecepatan(kecepatan)
        }
    }

    fun muat(videoId: String) {
        viewModelScope.launch {
            _keadaan.value = KeadaanMuat.Memuat
            val hasil = runCatching { YouTubeRepository.detail(videoId) }
            if (hasil.isSuccess) {
                val detail = hasil.getOrNull()
                _detail.value = detail
                _keadaan.value = KeadaanMuat.Berhasil
                if (detail != null) {
                    putarDenganAntrian(detail)
                }
            } else {
                _keadaan.value = KeadaanMuat.Gagal(
                    hasil.exceptionOrNull()?.message ?: "Video tidak dapat dimutar."
                )
            }
        }
    }

    private fun putarDenganAntrian(detail: VideoDetail) {
        val utama = PlayableVideo(
            item = detail.toVideoItem(),
            videoStreams = detail.videoStreams,
            audioStreams = detail.audioStreams
        )
        val terkait = detail.related.map { video ->
            PlayableVideo(
                item = video,
                videoStreams = emptyList(),
                audioStreams = emptyList()
            )
        }
        // Video utama memuat stream lengkap; video terkait mengikuti di antrian.
        playbackController.putar(
            daftar = listOf(utama) + terkait,
            indeksAwal = 0,
            kualitas = kualitasSaatIni ?: "720p"
        )
    }

    fun bukaDialogKualitas() { _tampilKualitas.value = true }
    fun tutupDialogKualitas() { _tampilKualitas.value = false }
    fun bukaDialogKecepatan() { _tampilKecepatan.value = true }
    fun tutupDialogKecepatan() { _tampilKecepatan.value = false }

    fun pilihKualitas(kualitas: String) {
        kualitasSaatIni = kualitas
        playbackController.pilihKualitas(kualitas)
        _tampilKualitas.value = false
        viewModelScope.launch { settingsRepository.simpanKualitas(kualitas) }
    }

    fun pilihKecepatan(kecepatan: Float) {
        playbackController.aturKecepatan(kecepatan)
        _tampilKecepatan.value = false
        viewModelScope.launch { settingsRepository.simpanKecepatan(kecepatan) }
    }

    fun jedaLanjut() = playbackController.jedaLanjut()
    fun lanjut() = playbackController.lanjut()
    fun sebelumnya() = playbackController.sebelumnya()
    fun lompatKe(persen: Float) = playbackController.lompatKe(persen)

    fun tutup() = playbackController.tutup()

    private fun VideoDetail.toVideoItem() = VideoItem(
        id = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        durationSeconds = durationSeconds,
        viewCount = viewCount,
        uploaderName = uploaderName,
        uploaderUrl = uploaderUrl,
        uploaderAvatarUrl = uploaderAvatarUrl,
        uploadDate = uploadDate
    )
}
