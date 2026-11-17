package com.nontontv.indonesia.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nontontv.indonesia.data.model.CommentItem
import com.nontontv.indonesia.data.model.VideoDetail
import com.nontontv.indonesia.data.model.VideoItem
import com.nontontv.indonesia.data.remote.youtube.YouTubeRepository
import com.nontontv.indonesia.data.repository.LibraryRepository
import com.nontontv.indonesia.ui.home.KeadaanMuat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _detail = MutableStateFlow<VideoDetail?>(null)
    val detail: StateFlow<VideoDetail?> = _detail.asStateFlow()

    private val _komentar = MutableStateFlow<List<CommentItem>>(emptyList())
    val komentar: StateFlow<List<CommentItem>> = _komentar.asStateFlow()

    private val _keadaan = MutableStateFlow<KeadaanMuat>(KeadaanMuat.Memuat)
    val keadaan: StateFlow<KeadaanMuat> = _keadaan.asStateFlow()

    private val _favorit = MutableStateFlow(false)
    val favorit: StateFlow<Boolean> = _favorit.asStateFlow()

    fun muat(videoId: String) {
        viewModelScope.launch {
            _keadaan.value = KeadaanMuat.Memuat
            val hasil = runCatching { YouTubeRepository.detail(videoId) }
            if (hasil.isSuccess) {
                _detail.value = hasil.getOrNull()
                _favorit.value = libraryRepository.apakahFavorit(videoId)
                _keadaan.value = KeadaanMuat.Berhasil
                muatKomentar(videoId)
            } else {
                _keadaan.value = KeadaanMuat.Gagal(
                    hasil.exceptionOrNull()?.message ?: "Video tidak dapat dimuat."
                )
            }
        }
    }

    private fun muatKomentar(videoId: String) {
        viewModelScope.launch {
            _komentar.value = runCatching { YouTubeRepository.komentar(videoId) }
                .getOrDefault(emptyList())
        }
    }

    fun muatUlang(videoId: String) = muat(videoId)

    fun beralihFavorit() {
        val detail = _detail.value ?: return
        viewModelScope.launch {
            if (_favorit.value) {
                libraryRepository.hapusFavorit(detail.id)
                _favorit.value = false
            } else {
                libraryRepository.tambahFavorit(detail.toVideoItem())
                _favorit.value = true
            }
        }
    }

    /** Konversi detail ke VideoItem untuk favorit/berbagi. */
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
