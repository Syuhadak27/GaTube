package com.nontontv.indonesia.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nontontv.indonesia.data.local.SearchHistoryEntity
import com.nontontv.indonesia.data.model.VideoItem
import com.nontontv.indonesia.data.remote.youtube.YouTubeRepository
import com.nontontv.indonesia.data.repository.LibraryRepository
import com.nontontv.indonesia.ui.home.KeadaanMuat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _hasil = MutableStateFlow<List<VideoItem>>(emptyList())
    val hasil: StateFlow<List<VideoItem>> = _hasil.asStateFlow()

    private val _saran = MutableStateFlow<List<String>>(emptyList())
    val saran: StateFlow<List<String>> = _saran.asStateFlow()

    private val _keadaan = MutableStateFlow<KeadaanMuat>(KeadaanMuat.Berhasil)
    val keadaan: StateFlow<KeadaanMuat> = _keadaan.asStateFlow()

    val riwayatPencarian: StateFlow<List<SearchHistoryEntity>> =
        libraryRepository.riwayatPencarian.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
        )

    fun ubahQuery(nilai: String) {
        _query.value = nilai
        if (nilai.trim().length >= 2) {
            cariSaran(nilai.trim())
        } else {
            _saran.value = emptyList()
        }
    }

    fun cari() {
        val kata = _query.value.trim()
        if (kata.isEmpty()) return
        viewModelScope.launch {
            _keadaan.value = KeadaanMuat.Memuat
            val hasilPencarian = runCatching { YouTubeRepository.cari(kata) }
            _hasil.value = hasilPencarian.getOrDefault(emptyList())
            _keadaan.value = if (hasilPencarian.isSuccess) {
                KeadaanMuat.Berhasil
            } else {
                KeadaanMuat.Gagal(
                    hasilPencarian.exceptionOrNull()?.message ?: "Pencarian gagal dimuat."
                )
            }
            libraryRepository.simpanPencarian(kata)
        }
    }

    fun cariDariSaran(kata: String) {
        _query.value = kata
        cari()
    }

    fun pilihRiwayat(kata: String) {
        _query.value = kata
        cari()
    }

    fun hapusRiwayat(kata: String) {
        viewModelScope.launch { libraryRepository.hapusPencarian(kata) }
    }

    fun bersihkanRiwayat() {
        viewModelScope.launch { libraryRepository.bersihkanPencarian() }
    }

    private fun cariSaran(kata: String) {
        viewModelScope.launch {
            _saran.value = runCatching { YouTubeRepository.saran(kata) }
                .getOrDefault(emptyList())
        }
    }
}
