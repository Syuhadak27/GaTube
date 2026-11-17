package com.nontontv.indonesia.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nontontv.indonesia.data.model.PlaylistDetail
import com.nontontv.indonesia.data.remote.youtube.YouTubeRepository
import com.nontontv.indonesia.ui.home.KeadaanMuat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor() : ViewModel() {

    private val _detail = MutableStateFlow<PlaylistDetail?>(null)
    val detail: StateFlow<PlaylistDetail?> = _detail.asStateFlow()

    private val _keadaan = MutableStateFlow<KeadaanMuat>(KeadaanMuat.Memuat)
    val keadaan: StateFlow<KeadaanMuat> = _keadaan.asStateFlow()

    fun muat(url: String) {
        viewModelScope.launch {
            _keadaan.value = KeadaanMuat.Memuat
            val hasil = runCatching { YouTubeRepository.playlist(url) }
            _detail.value = hasil.getOrNull()
            _keadaan.value = if (hasil.isSuccess) {
                KeadaanMuat.Berhasil
            } else {
                KeadaanMuat.Gagal(
                    hasil.exceptionOrNull()?.message ?: "Playlist tidak dapat dimuat."
                )
            }
        }
    }

    fun muatUlang(url: String) = muat(url)
}
