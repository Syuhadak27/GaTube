package com.nontontv.indonesia.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nontontv.indonesia.BuildConfig
import com.nontontv.indonesia.data.local.SessionManager
import com.nontontv.indonesia.data.local.SettingsRepository
import com.nontontv.indonesia.data.model.Account
import com.nontontv.indonesia.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val akun: StateFlow<Account?> = sessionManager.akun
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sudahMasuk: StateFlow<Boolean> = sessionManager.sudahMasuk
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val kualitasDefault: StateFlow<String> = settingsRepository.kualitasDefault
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "720p")

    val kecepatanDefault: StateFlow<Float> = settingsRepository.kecepatanDefault
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1.0f)

    val pipOtomatis: StateFlow<Boolean> = settingsRepository.pipOtomatis
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val backgroundPlay: StateFlow<Boolean> = settingsRepository.backgroundPlay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val pairingTersedia: StateFlow<Boolean> =
        MutableStateFlow(accountRepository.pairingTersedia()).asStateFlow()

    /** Contoh: "v1.0.0 (12)". */
    val versiTerpasang: String = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

    fun keluar() {
        viewModelScope.launch { accountRepository.keluar() }
    }

    fun simpanKualitas(kualitas: String) {
        viewModelScope.launch { settingsRepository.simpanKualitas(kualitas) }
    }

    fun simpanKecepatan(kecepatan: Float) {
        viewModelScope.launch { settingsRepository.simpanKecepatan(kecepatan) }
    }

    fun simpanPipOtomatis(aktif: Boolean) {
        viewModelScope.launch { settingsRepository.simpanPipOtomatis(aktif) }
    }

    fun simpanBackgroundPlay(aktif: Boolean) {
        viewModelScope.launch { settingsRepository.simpanBackgroundPlay(aktif) }
    }
}
