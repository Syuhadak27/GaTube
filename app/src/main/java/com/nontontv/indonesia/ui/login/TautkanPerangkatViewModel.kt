package com.nontontv.indonesia.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nontontv.indonesia.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Status penanganan kode dari perangkat lain. */
sealed interface StatusTautan {
    data object Idle : StatusTautan
    data object Memproses : StatusTautan
    data object Berhasil : StatusTautan
    data class Gagal(val pesan: String) : StatusTautan
}

/**
 * Layar "Tautkan Perangkat Baru": memasukkan kode lokal lalu menyetujuinya.
 * Sesi login disimpan di perangkat ini.
 */
@HiltViewModel
class TautkanPerangkatViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _kode = MutableStateFlow("")
    val kode: StateFlow<String> = _kode.asStateFlow()

    private val _status = MutableStateFlow<StatusTautan>(StatusTautan.Idle)
    val status: StateFlow<StatusTautan> = _status.asStateFlow()

    fun ubahKode(nilai: String) {
        _kode.value = nilai.uppercase().filter { it.isLetterOrDigit() || it == '-' }.take(9)
    }

    fun setujui() {
        val kode = _kode.value.trim()
        if (kode.length < 8) {
            _status.value = StatusTautan.Gagal("Kode minimal 8 karakter, contoh: ABCD-1234")
            return
        }
        viewModelScope.launch {
            _status.value = StatusTautan.Memproses
            val akun = accountRepository.akunSaatIni()
            val token = akun?.token ?: AccountRepository.buatTokenAcak()
            val hasil = accountRepository.setujuiKode(kode, token)
            _status.value = if (hasil.isSuccess) {
                StatusTautan.Berhasil
            } else {
                StatusTautan.Gagal(
                    hasil.exceptionOrNull()?.message
                        ?: "Gagal menautkan. Pastikan kode masih berlaku."
                )
            }
        }
    }
}
