package com.nontontv.indonesia.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nontontv.indonesia.ui.components.DialogKecepatan
import com.nontontv.indonesia.ui.components.DialogPembaruan
import com.nontontv.indonesia.ui.navigasi.Rute
import com.nontontv.indonesia.ui.update.UpdateViewModel

private val daftarKualitas = listOf("144p", "240p", "360p", "480p", "720p", "1080p", "1440p", "4K")

/** Layar Pengaturan: akun, putar, dan tentang aplikasi. */
@Composable
fun SettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel = viewModel()
    val updateViewModel: UpdateViewModel = viewModel()

    val akun by viewModel.akun.collectAsStateWithLifecycle()
    val sudahMasuk by viewModel.sudahMasuk.collectAsStateWithLifecycle()
    val kualitasDefault by viewModel.kualitasDefault.collectAsStateWithLifecycle()
    val kecepatanDefault by viewModel.kecepatanDefault.collectAsStateWithLifecycle()
    val pipOtomatis by viewModel.pipOtomatis.collectAsStateWithLifecycle()
    val backgroundPlay by viewModel.backgroundPlay.collectAsStateWithLifecycle()
    val pairingTersedia by viewModel.pairingTersedia.collectAsStateWithLifecycle()

    val updateInfo by updateViewModel.updateInfo.collectAsStateWithLifecycle()
    val sedangMengunduh by updateViewModel.sedangMengunduh.collectAsStateWithLifecycle()
    val progres by updateViewModel.progres.collectAsStateWithLifecycle()
    val perluIzin by updateViewModel.perluIzin.collectAsStateWithLifecycle()
    val pesanUpdate by updateViewModel.pesan.collectAsStateWithLifecycle()

    var tampilKualitas by remember { mutableStateOf(false) }
    var tampilKecepatan by remember { mutableStateOf(false) }

    LaunchedEffect(perluIzin) {
        if (perluIzin) {
            updateViewModel.bukaPengaturanIzin()
            updateViewModel.konsumsiIzin()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
    ) {
        // ---- Akun ----
        item {
            JudulBagian("Akun")
            if (sudahMasuk && akun != null) {
                BarisInfo(
                    judul = "Masuk sebagai",
                    nilai = akun?.deviceName?.ifBlank { "Perangkat Terhubung" } ?: "Perangkat Terhubung"
                )
                BarisInfo(judul = "Token", nilai = "•••••••• (tersimpan terenkripsi)")
            } else {
                BarisInfo(judul = "Status", nilai = "Mode Tamu")
            }
            BarisAksi(
                ikon = Icons.Filled.Link,
                judul = "Tautkan Perangkat Baru",
                keterangan = if (pairingTersedia) "Masukkan kode lokal dari perangkat ini"
                else "Pairing lokal siap dipakai",
                padaKlik = { navController.navigate(Rute.TAUTAN) },
                diaktifkan = pairingTersedia
            )
            if (sudahMasuk) {
                BarisAksi(
                    ikon = Icons.Filled.Logout,
                    judul = "Keluar",
                    keterangan = "Hapus sesi login dari perangkat ini",
                    padaKlik = viewModel::keluar
                )
            }
        }

        // ---- Putar ----
        item {
            JudulBagian("Putar")
            BarisAksi(
                judul = "Kualitas Video Bawaan",
                keterangan = kualitasDefault,
                padaKlik = { tampilKualitas = true }
            )
            BarisAksi(
                judul = "Kecepatan Putar Bawaan",
                keterangan = "${kecepatanDefault}x",
                padaKlik = { tampilKecepatan = true }
            )
            BarisSaklar(
                judul = "Picture-in-Picture Otomatis",
                keterangan = "Masuk PiP saat aplikasi di-minimize",
                tercentang = pipOtomatis,
                padaPerubahan = viewModel::simpanPipOtomatis
            )
            BarisSaklar(
                judul = "Background Play",
                keterangan = "Audio tetap jalan saat layar terkunci",
                tercentang = backgroundPlay,
                padaPerubahan = viewModel::simpanBackgroundPlay
            )
        }

        // ---- Tentang ----
        item {
            JudulBagian("Tentang Aplikasi")
            BarisInfo(judul = "Versi Terpasang", nilai = viewModel.versiTerpasang)
            BarisAksi(
                ikon = Icons.Filled.SystemUpdate,
                judul = "Cek Pembaruan",
                keterangan = "Ambil info rilis terbaru dari GitHub",
                padaKlik = { updateViewModel.periksa(tampilkanPesan = true) }
            )
            if (pesanUpdate != null) {
                Text(
                    text = pesanUpdate.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "GaTube • YouTube tanpa iklan, PiP, dan background play.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    if (tampilKualitas) {
        AlertDialog(
            onDismissRequest = { tampilKualitas = false },
            title = { Text("Kualitas Video Bawaan") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(daftarKualitas) { kualitas ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.simpanKualitas(kualitas)
                                    tampilKualitas = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = kualitas == kualitasDefault,
                                onClick = {
                                    viewModel.simpanKualitas(kualitas)
                                    tampilKualitas = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(kualitas, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { tampilKualitas = false }) { Text("Tutup") }
            }
        )
    }

    if (tampilKecepatan) {
        DialogKecepatan(
            kecepatanTerpilih = kecepatanDefault,
            padaPilih = { viewModel.simpanKecepatan(it); tampilKecepatan = false },
            padaTutup = { tampilKecepatan = false }
        )
    }

    updateInfo?.let { info ->
        DialogPembaruan(
            versiBaru = info.versionName,
            changelog = info.changelog,
            sedangMengunduh = sedangMengunduh,
            progres = progres,
            perluIzin = perluIzin,
            pesan = pesanUpdate,
            padaPerbarui = updateViewModel::mulaiUnduh,
            padaNanti = updateViewModel::tutupDialog
        )
    }
}

@Composable
private fun JudulBagian(teks: String) {
    Text(
        text = teks,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun BarisInfo(judul: String, nilai: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = judul,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = nilai,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BarisAksi(
    judul: String,
    keterangan: String,
    padaKlik: () -> Unit,
    ikon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    diaktifkan: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = diaktifkan) { padaKlik() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (ikon != null) {
            Icon(
                ikon,
                contentDescription = null,
                tint = if (diaktifkan) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = judul,
                style = MaterialTheme.typography.bodyLarge,
                color = if (diaktifkan) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                text = keterangan,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BarisSaklar(
    judul: String,
    keterangan: String,
    tercentang: Boolean,
    padaPerubahan: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = judul, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = keterangan,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = tercentang, onCheckedChange = padaPerubahan)
    }
}
