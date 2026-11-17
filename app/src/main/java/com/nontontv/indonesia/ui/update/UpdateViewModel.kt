package com.nontontv.indonesia.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nontontv.indonesia.data.model.UpdateInfo
import com.nontontv.indonesia.data.repository.UpdateRepository
import com.nontontv.indonesia.util.UpdateDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Mengecek pembaruan dari GitHub Release, mengunduh APK, dan membuka installer.
 */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val updateDownloader: UpdateDownloader
) : ViewModel() {

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val _sedangMengunduh = MutableStateFlow(false)
    val sedangMengunduh: StateFlow<Boolean> = _sedangMengunduh.asStateFlow()

    private val _progres = MutableStateFlow(0)
    val progres: StateFlow<Int> = _progres.asStateFlow()

    private val _perluIzin = MutableStateFlow(false)
    val perluIzin: StateFlow<Boolean> = _perluIzin.asStateFlow()

    private val _pesan = MutableStateFlow<String?>(null)
    val pesan: StateFlow<String?> = _pesan.asStateFlow()

    /** Dipanggil saat aplikasi dibuka dan saat tombol "Cek Pembaruan". */
    fun periksa(tampilkanPesan: Boolean = false) {
        viewModelScope.launch {
            val hasil = runCatching { updateRepository.periksaPembaruan() }
            val info = hasil.getOrNull()
            _updateInfo.value = info
            _pesan.value = when {
                hasil.isFailure ->
                    "Gagal memeriksa pembaruan: ${hasil.exceptionOrNull()?.message}"
                info == null && tampilkanPesan ->
                    "Kamu sudah menggunakan versi terbaru."
                else -> null
            }
        }
    }

    /** Memulai unduhan APK. Menolak bila izin install belum diberikan. */
    fun mulaiUnduh() {
        val info = _updateInfo.value ?: return
        if (!updateDownloader.bisaInstallAplikasiTakDikenal()) {
            _perluIzin.value = true
            _pesan.value = "Izinkan instalasi aplikasi tak dikenal untuk melanjutkan."
            return
        }
        if (_sedangMengunduh.value) return

        _sedangMengunduh.value = true
        _progres.value = 0
        _pesan.value = null
        viewModelScope.launch {
            try {
                val berkas = updateDownloader.unduhApk(info.apkUrl) { progres ->
                    _progres.value = progres
                }
                _sedangMengunduh.value = false
                _updateInfo.value = null
                updateDownloader.bukaInstaller(berkas)
            } catch (e: Exception) {
                _sedangMengunduh.value = false
                _pesan.value = e.message ?: "Unduhan gagal."
            }
        }
    }

    fun bukaPengaturanIzin() {
        updateDownloader.bukaPengaturanInstall()
    }

    /** Menandai bahwa permintaan izin sudah ditindaklanjuti. */
    fun konsumsiIzin() {
        _perluIzin.value = false
    }

    fun tutupDialog() {
        _updateInfo.value = null
        _perluIzin.value = false
    }

    fun bersihkanPesan() {
        _pesan.value = null
    }
}
