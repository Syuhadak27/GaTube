package com.nontontv.indonesia.ui.login

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nontontv.indonesia.data.repository.AccountRepository
import com.nontontv.indonesia.util.Config
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Status proses pairing kode. */
sealed interface StatusPairing {
    data object Menunggu : StatusPairing
    data object Berhasil : StatusPairing
    data object Kedaluwarsa : StatusPairing
    data class Gagal(val pesan: String) : StatusPairing
}

/**
 * Membuat kode pairing lokal lalu memantau statusnya.
 * Kode disimpan di perangkat; proses persetujuan dilakukan di layar
 * "Tautkan Perangkat Baru" yang membaca kode yang sama.
 */
@HiltViewModel
class PairingViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _status = MutableStateFlow<StatusPairing>(StatusPairing.Menunggu)
    val status: StateFlow<StatusPairing> = _status.asStateFlow()

    private var tugas: Job? = null

    /** Membuat kode lokal lalu memantau sampai disetujui atau kedaluwarsa. */
    fun mulai(kode: String) {
        tugas?.cancel()
        tugas = viewModelScope.launch {
            _status.value = StatusPairing.Menunggu

            val hasilBuat = accountRepository.buatKode(kode)
            if (hasilBuat.isFailure) {
                _status.value = StatusPairing.Gagal(
                    hasilBuat.exceptionOrNull()?.message ?: "Gagal membuat kode lokal."
                )
                return@launch
            }

            val batasWaktu = System.currentTimeMillis() + Config.PAIRING_EXPIRED_DETIK * 1000L
            while (System.currentTimeMillis() < batasWaktu) {
                delay(2_000L)
                val dto = accountRepository.ambilKode(kode)
                if (dto == null) continue

                when (dto.status) {
                    AccountRepository.STATUS_APPROVED -> {
                        val token = dto.token
                        if (!token.isNullOrBlank()) {
                            accountRepository.masukDenganToken(token, namaPerangkat())
                        }
                        accountRepository.hapusKodePairing()
                        _status.value = StatusPairing.Berhasil
                        return@launch
                    }
                    AccountRepository.STATUS_PENDING -> {
                        _status.value = StatusPairing.Menunggu
                    }
                }
            }
            _status.value = StatusPairing.Kedaluwarsa
        }
    }

    fun berhenti() {
        tugas?.cancel()
    }

    private fun namaPerangkat(): String {
        val merek = Build.MANUFACTURER?.replaceFirstChar { it.uppercase() } ?: "Perangkat"
        val model = Build.MODEL ?: ""
        return "$merek $model".trim()
    }
}
