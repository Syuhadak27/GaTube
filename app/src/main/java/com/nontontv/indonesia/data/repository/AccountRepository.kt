package com.nontontv.indonesia.data.repository

import com.nontontv.indonesia.data.local.SessionManager
import com.nontontv.indonesia.data.model.Account
import com.nontontv.indonesia.data.model.KodePairingLokal
import com.nontontv.indonesia.util.Config
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Login/pairing lokal.
 *
 * Tidak memakai server. Kode pairing disimpan di DataStore perangkat ini,
 * lalu sesi login (token) juga disimpan di DataStore terenkripsi.
 * Dengan demikian semua data akun bersifat lokal.
 */
@Singleton
class AccountRepository @Inject constructor(
    private val sessionManager: SessionManager
) {

    /** Pairing lokal selalu tersedia. */
    fun pairingTersedia(): Boolean = true

    /** Menyimpan kode pending di perangkat. */
    suspend fun buatKode(kode: String): Result<Unit> = runCatching {
        sessionManager.simpanKodePairing(
            kode = kode,
            status = STATUS_PENDING,
            kedaluwarsa = System.currentTimeMillis() + Config.PAIRING_EXPIRED_DETIK * 1000L
        )
    }

    /** Membaca kode yang tersimpan; null bila kode tidak cocok. */
    suspend fun ambilKode(kode: String): KodePairingLokal? {
        val tersimpan = sessionManager.ambilKodePairing() ?: return null
        return if (tersimpan.kode == kode) tersimpan else null
    }

    /** Menyetujui kode lalu membuat sesi lokal. */
    suspend fun setujuiKode(kode: String, token: String): Result<Unit> = runCatching {
        val tersimpan = sessionManager.ambilKodePairing()
        if (tersimpan == null || tersimpan.kode != kode) {
            error("Kode tidak dikenal. Buat kode baru terlebih dahulu.")
        }
        if (System.currentTimeMillis() > tersimpan.kedaluwarsa) {
            error("Kode sudah kedaluwarsa. Buat kode baru.")
        }
        sessionManager.simpanAkun(
            Account(
                token = token,
                deviceName = "Perangkat Lokal",
                linkedAt = System.currentTimeMillis()
            )
        )
        Unit
    }

    suspend fun akunSaatIni(): Account? = sessionManager.ambilAkun()

    suspend fun masukDenganToken(token: String, namaPerangkat: String) {
        sessionManager.simpanAkun(
            Account(
                token = token,
                deviceName = namaPerangkat,
                linkedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun keluar() = sessionManager.keluar()

    /** Membersihkan kode setelah pairing selesai. */
    suspend fun hapusKodePairing() = sessionManager.hapusKodePairing()

    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_APPROVED = "approved"

        /** Membuat kode 8 karakter dengan format ABCD-1234. */
        fun buatKodeAcak(): String {
            val huruf = "ABCDEFGHJKLMNPQRSTUVWXYZ"
            val angka = "0123456789"
            val acak = SecureRandom()
            val bagianHuruf = (1..4).map { huruf[acak.nextInt(huruf.length)] }.joinToString("")
            val bagianAngka = (1..4).map { angka[acak.nextInt(angka.length)] }.joinToString("")
            return "$bagianHuruf-$bagianAngka"
        }

        /** Token acak untuk sesi lokal. */
        fun buatTokenAcak(): String =
            java.util.UUID.randomUUID().toString().replace("-", "")
    }
}
