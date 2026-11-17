package com.nontontv.indonesia.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nontontv.indonesia.data.model.StreamOption

/** Dialog untuk memilih kualitas video. */
@Composable
fun DialogKualitas(
    daftarStream: List<StreamOption>,
    kualitasTerpilih: String,
    padaPilih: (String) -> Unit,
    padaTutup: () -> Unit
) {
    AlertDialog(
        onDismissRequest = padaTutup,
        title = { Text("Pilih Kualitas") },
        text = {
            LazyColumn(modifier = Modifier.height(320.dp)) {
                items(daftarStream) { stream ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { padaPilih(stream.quality) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = stream.quality == kualitasTerpilih,
                            onClick = { padaPilih(stream.quality) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stream.quality,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            val info = buildString {
                                if (stream.height > 0) append("${stream.height}p")
                                if (stream.fps > 0) append(" • ${stream.fps} fps")
                                if (stream.isVideoOnly) append(" • video only")
                            }
                            if (info.isNotBlank()) {
                                Text(
                                    text = info,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = padaTutup) { Text("Tutup") }
        }
    )
}

private val daftarKecepatan = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

/** Dialog untuk memilih kecepatan putar. */
@Composable
fun DialogKecepatan(
    kecepatanTerpilih: Float,
    padaPilih: (Float) -> Unit,
    padaTutup: () -> Unit
) {
    AlertDialog(
        onDismissRequest = padaTutup,
        title = { Text("Kecepatan Putar") },
        text = {
            LazyColumn(modifier = Modifier.height(320.dp)) {
                items(daftarKecepatan) { kecepatan ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { padaPilih(kecepatan) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = kecepatan == kecepatanTerpilih,
                            onClick = { padaPilih(kecepatan) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (kecepatan == 1.0f) "1x (Normal)" else "${kecepatan}x",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = padaTutup) { Text("Tutup") }
        }
    )
}

/**
 * Dialog pembaruan versi baru.
 * Menampilkan versi, changelog, progres unduhan, dan tombol aksi.
 */
@Composable
fun DialogPembaruan(
    versiBaru: String,
    changelog: String,
    sedangMengunduh: Boolean,
    progres: Int,
    perluIzin: Boolean = false,
    pesan: String? = null,
    padaPerbarui: () -> Unit,
    padaNanti: () -> Unit
) {
    AlertDialog(
        onDismissRequest = padaNanti,
        title = {
            Text(
                text = "Versi Baru Tersedia!",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column {
                Text(
                    text = "Versi v$versiBaru sudah tersedia. Silakan perbarui untuk " +
                        "fitur terbaru dan perbaikan bug.",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (changelog.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Catatan Perubahan:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = changelog,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (sedangMengunduh) {
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progres / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Mengunduh... $progres%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (pesan != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = pesan,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = padaPerbarui,
                enabled = !sedangMengunduh
            ) {
                Text(
                    when {
                        sedangMengunduh -> "Mengunduh..."
                        perluIzin -> "Buka Pengaturan Izin"
                        else -> "Perbarui Sekarang"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = padaNanti, enabled = !sedangMengunduh) {
                Text("Nanti")
            }
        }
    )
}
