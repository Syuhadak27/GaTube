package com.nontontv.indonesia.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nontontv.indonesia.data.local.SessionManager
import com.nontontv.indonesia.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Status tampilan login. */
sealed interface StatusLogin {
    /** Pengguna melihat layar pilihan masuk. */
    data object Pilih : StatusLogin

    /** Layar pairing dengan kode yang dihasilkan perangkat ini. */
    data class Pairing(val kode: String) : StatusLogin
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val accountRepository: AccountRepository
) : ViewModel() {

    /** null = belum dimuat, false = belum pernah membuka login, true = lanjut. */
    val sudahLihatLogin: StateFlow<Boolean?> = sessionManager.sudahLihatLogin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sudahMasuk: StateFlow<Boolean> = sessionManager.sudahMasuk
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val pairingTersedia: StateFlow<Boolean> =
        MutableStateFlow(accountRepository.pairingTersedia()).asStateFlow()

    private val _keadaan = MutableStateFlow<StatusLogin>(StatusLogin.Pilih)
    val keadaan: StateFlow<StatusLogin> = _keadaan.asStateFlow()

    fun tandaiSudahLihatLogin() {
        viewModelScope.launch { sessionManager.tandaiSudahLihatLogin() }
    }

    /** Dipanggil dari layar login: pengguna memilih "Masuk dengan Kode". */
    fun mulaiPairing() {
        val kode = AccountRepository.buatKodeAcak()
        _keadaan.value = StatusLogin.Pairing(kode)
    }

    fun buatUlangKode() {
        val kode = AccountRepository.buatKodeAcak()
        _keadaan.value = StatusLogin.Pairing(kode)
    }

    fun keluarDariPairing() {
        _keadaan.value = StatusLogin.Pilih
    }
}
