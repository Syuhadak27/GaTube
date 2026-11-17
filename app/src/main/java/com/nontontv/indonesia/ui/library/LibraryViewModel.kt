package com.nontontv.indonesia.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nontontv.indonesia.data.local.FavoriteEntity
import com.nontontv.indonesia.data.local.HistoryEntity
import com.nontontv.indonesia.data.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    val riwayat: StateFlow<List<HistoryEntity>> = libraryRepository.riwayat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favorit: StateFlow<List<FavoriteEntity>> = libraryRepository.favorit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun hapusRiwayat(videoId: String) {
        viewModelScope.launch { libraryRepository.hapusRiwayat(videoId) }
    }

    fun bersihkanRiwayat() {
        viewModelScope.launch { libraryRepository.bersihkanRiwayat() }
    }

    fun hapusFavorit(videoId: String) {
        viewModelScope.launch { libraryRepository.hapusFavorit(videoId) }
    }
}
