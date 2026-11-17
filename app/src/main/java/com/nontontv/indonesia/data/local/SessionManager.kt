package com.nontontv.indonesia.data.local

import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nontontv.indonesia.data.model.Account
import com.nontontv.indonesia.data.model.KodePairingLokal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

/**
 * Menyimpan sesi login di DataStore, tetapi nilai token dienkripsi
 * memakai kunci AES-256 GCM dari AndroidKeyStore.
 *
 * Jadi file DataStore yang bocor pun tidak berisi token dalam bentuk teks.
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore = context.sessionDataStore

    private val kunciToken = stringPreferencesKey("token_terenkripsi")
    private val kunciPerangkat = stringPreferencesKey("perangkat_terenkripsi")
    private val kunciWaktu = longPreferencesKey("waktu_terhubung")
    private val kunciLihatLogin = booleanPreferencesKey("sudah_lihat_login")

    private val kunciKodePairing = stringPreferencesKey("pairing_kode")
    private val kunciStatusPairing = stringPreferencesKey("pairing_status")
    private val kunciKedaluwarsaPairing = longPreferencesKey("pairing_kedaluwarsa")
    private val kunciTokenPairing = stringPreferencesKey("pairing_token")

    val akun: Flow<Account?> = dataStore.data.map { pref ->
        val token = pref[kunciToken]?.let { dekripsi(it) }
        if (token.isNullOrBlank()) {
            null
        } else {
            Account(
                token = token,
                deviceName = pref[kunciPerangkat]?.let { dekripsi(it) }.orEmpty(),
                linkedAt = pref[kunciWaktu] ?: 0L
            )
        }
    }

    val sudahMasuk: Flow<Boolean> = akun.map { it != null }

    val sudahLihatLogin: Flow<Boolean> = dataStore.data.map { pref ->
        pref[kunciLihatLogin] ?: false
    }

    suspend fun simpanAkun(akun: Account) {
        dataStore.edit { pref ->
            pref[kunciToken] = enkripsi(akun.token)
            pref[kunciPerangkat] = enkripsi(akun.deviceName)
            pref[kunciWaktu] = akun.linkedAt
            pref[kunciLihatLogin] = true
        }
    }

    suspend fun keluar() {
        dataStore.edit { pref ->
            pref.remove(kunciToken)
            pref.remove(kunciPerangkat)
            pref.remove(kunciWaktu)
        }
    }

    suspend fun tandaiSudahLihatLogin() {
        dataStore.edit { pref -> pref[kunciLihatLogin] = true }
    }

    suspend fun ambilAkun(): Account? = akun.first()

    // ---- Kode pairing lokal (tanpa server) ----

    suspend fun simpanKodePairing(
        kode: String,
        status: String,
        kedaluwarsa: Long,
        token: String? = null
    ) {
        dataStore.edit { pref ->
            pref[kunciKodePairing] = kode
            pref[kunciStatusPairing] = status
            pref[kunciKedaluwarsaPairing] = kedaluwarsa
            if (token.isNullOrBlank()) {
                pref.remove(kunciTokenPairing)
            } else {
                pref[kunciTokenPairing] = token
            }
        }
    }

    suspend fun ambilKodePairing(): KodePairingLokal? {
        val pref = dataStore.data.first()
        val kode = pref[kunciKodePairing] ?: return null
        return KodePairingLokal(
            kode = kode,
            status = pref[kunciStatusPairing] ?: "pending",
            token = pref[kunciTokenPairing],
            kedaluwarsa = pref[kunciKedaluwarsaPairing] ?: 0L
        )
    }

    suspend fun hapusKodePairing() {
        dataStore.edit { pref ->
            pref.remove(kunciKodePairing)
            pref.remove(kunciStatusPairing)
            pref.remove(kunciKedaluwarsaPairing)
            pref.remove(kunciTokenPairing)
        }
    }

    // ---- Enkripsi AES-GCM dengan AndroidKeyStore ----

    private val kunciRahasia: SecretKey by lazy { muatAtauBuatKunci() }

    private fun muatAtauBuatKunci(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val entri = keyStore.getEntry(NAMA_KUNCI, null)
        if (entri is KeyStore.SecretKeyEntry) {
            return entri.secretKey
        }
        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        generator.init(
            KeyGenParameterSpec.Builder(
                NAMA_KUNCI,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private fun enkripsi(teks: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, kunciRahasia)
        val iv = cipher.iv
        val terenkripsi = cipher.doFinal(teks.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(iv + terenkripsi, Base64.NO_WRAP)
    }

    private fun dekripsi(teks: String): String {
        val data = Base64.decode(teks, Base64.NO_WRAP)
        require(data.size > 12) { "Data terenkripsi tidak valid" }
        val iv = data.copyOfRange(0, 12)
        val muatan = data.copyOfRange(12, data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, kunciRahasia, GCMParameterSpec(128, iv))
        return String(cipher.doFinal(muatan), Charsets.UTF_8)
    }

    private companion object {
        const val NAMA_KUNCI = "GaTubeSessionKey"
    }
}
