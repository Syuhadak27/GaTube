package com.nontontv.indonesia.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nontontv.indonesia.data.model.Kategori
import com.nontontv.indonesia.data.model.VideoItem
import com.nontontv.indonesia.data.remote.youtube.YouTubeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Keadaan pemuatan daftar video. */
sealed interface KeadaanMuat {
    data object Memuat : KeadaanMuat
    data object Berhasil : KeadaanMuat
    data class Gagal(val pesan: String) : KeadaanMuat
}

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _video = MutableStateFlow<List<VideoItem>>(emptyList())
    val video: StateFlow<List<VideoItem>> = _video.asStateFlow()

    private val _kategori = MutableStateFlow<List<Kategori>>(emptyList())
    val kategori: StateFlow<List<Kategori>> = _kategori.asStateFlow()

    private val _kategoriTerpilih = MutableStateFlow("Trending")
    val kategoriTerpilih: StateFlow<String> = _kategoriTerpilih.asStateFlow()

    private val _keadaan = MutableStateFlow<KeadaanMuat>(KeadaanMuat.Memuat)
    val keadaan: StateFlow<KeadaanMuat> = _keadaan.asStateFlow()

    init {
        muatKategori()
        muatVideo("Trending")
    }

    private fun muatKategori() {
        _kategori.value = runCatching { YouTubeRepository.daftarKategori() }
            .getOrDefault(emptyList())
    }

    fun pilihKategori(id: String) {
        if (id == _kategoriTerpilih.value) return
        _kategoriTerpilih.value = id
        muatVideo(id)
    }

    fun muatUlang() {
        muatVideo(_kategoriTerpilih.value)
    }

    private fun muatVideo(idKategori: String) {
        viewModelScope.launch {
            _keadaan.value = KeadaanMuat.Memuat
            val hasil = runCatching {
                if (idKategori == "Trending") {
                    YouTubeRepository.trending()
                } else {
                    YouTubeRepository.videoKategori(idKategori)
                }
            }
            _video.value = hasil.getOrDefault(emptyList())
            _keadaan.value = if (hasil.isSuccess) {
                KeadaanMuat.Berhasil
            } else {
                KeadaanMuat.Gagal(
                    hasil.exceptionOrNull()?.message
                        ?: "Tidak dapat memuat video. Periksa koneksi internet."
                )
            }
        }
    }
}
