package com.nontontv.indonesia.ui.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nontontv.indonesia.data.model.ChannelDetail
import com.nontontv.indonesia.data.remote.youtube.YouTubeRepository
import com.nontontv.indonesia.ui.home.KeadaanMuat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor() : ViewModel() {

    private val _detail = MutableStateFlow<ChannelDetail?>(null)
    val detail: StateFlow<ChannelDetail?> = _detail.asStateFlow()

    private val _keadaan = MutableStateFlow<KeadaanMuat>(KeadaanMuat.Memuat)
    val keadaan: StateFlow<KeadaanMuat> = _keadaan.asStateFlow()

    fun muat(url: String) {
        viewModelScope.launch {
            _keadaan.value = KeadaanMuat.Memuat
            val hasil = runCatching { YouTubeRepository.kanal(url) }
            _detail.value = hasil.getOrNull()
            _keadaan.value = if (hasil.isSuccess) {
                KeadaanMuat.Berhasil
            } else {
                KeadaanMuat.Gagal(
                    hasil.exceptionOrNull()?.message ?: "Kanal tidak dapat dimuat."
                )
            }
        }
    }

    fun muatUlang(url: String) = muat(url)
}
