package com.nontontv.indonesia.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView
import com.nontontv.indonesia.ui.components.DialogKecepatan
import com.nontontv.indonesia.ui.components.DialogKualitas
import com.nontontv.indonesia.ui.components.IndikatorMemuat
import com.nontontv.indonesia.ui.components.PesanKesalahan
import com.nontontv.indonesia.ui.home.KeadaanMuat
import com.nontontv.indonesia.util.FormatUtils

/** Layar pemutar video dengan kontrol kualitas, kecepatan, dan navigasi. */
@Composable
fun PlayerScreen(
    videoId: String,
    padaTutup: () -> Unit
) {
    val viewModel: PlayerViewModel = viewModel()
    LaunchedEffect(videoId) { viewModel.muat(videoId) }

    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val keadaan by viewModel.keadaan.collectAsStateWithLifecycle()
    val sedangMemutar by viewModel.sedangMemutar.collectAsStateWithLifecycle()
    val posisi by viewModel.posisi.collectAsStateWithLifecycle()
    val durasi by viewModel.durasi.collectAsStateWithLifecycle()
    val kecepatan by viewModel.kecepatan.collectAsStateWithLifecycle()
    val kualitas by viewModel.kualitasTerpilih.collectAsStateWithLifecycle()
    val memuat by viewModel.memuat.collectAsStateWithLifecycle()
    val tampilKualitas by viewModel.tampilKualitas.collectAsStateWithLifecycle()
    val tampilKecepatan by viewModel.tampilKecepatan.collectAsStateWithLifecycle()

    var progresLokal by remember { mutableStateOf<Float?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = viewModel.player
                    useController = false
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        when (val k = keadaan) {
            KeadaanMuat.Memuat -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                IndikatorMemuat("Menyiapkan video...")
            }
            is KeadaanMuat.Gagal -> PesanKesalahan(
                pesan = k.pesan,
                onCobaLagi = { viewModel.muat(videoId) }
            )
            KeadaanMuat.Berhasil -> {}
        }

        // Bar atas: tombol tutup.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = { viewModel.tutup(); padaTutup() }) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Tutup",
                    tint = Color.White
                )
            }
        }

        // Bar bawah: kontrol pemutar.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xAA000000))
                .padding(12.dp)
        ) {
            detail?.let { d ->
                Text(
                    text = d.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = d.uploaderName,
                    color = Color(0xFFBBBBBB),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            val nilaiSlider = progresLokal
                ?: if (durasi > 0) posisi.toFloat() / durasi else 0f

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = FormatUtils.formatDurasi(posisi / 1000),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
                Slider(
                    value = nilaiSlider.coerceIn(0f, 1f),
                    onValueChange = { progresLokal = it },
                    onValueChangeFinished = {
                        viewModel.lompatKe(progresLokal ?: 0f)
                        progresLokal = null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )
                Text(
                    text = FormatUtils.formatDurasi(durasi / 1000),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = viewModel::bukaDialogKualitas) {
                    Text(kualitas)
                }
                OutlinedButton(onClick = viewModel::bukaDialogKecepatan) {
                    Text("${kecepatan}x")
                }
                IconButton(onClick = viewModel::sebelumnya) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Sebelumnya", tint = Color.White)
                }
                IconButton(onClick = viewModel::jedaLanjut, modifier = Modifier.size(56.dp)) {
                    if (memuat) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (sedangMemutar) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (sedangMemutar) "Jeda" else "Putar",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                IconButton(onClick = viewModel::lanjut) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Berikutnya", tint = Color.White)
                }
                Spacer(Modifier.width(48.dp))
            }
        }
    }

    if (tampilKualitas) {
        DialogKualitas(
            daftarStream = detail?.videoStreams.orEmpty(),
            kualitasTerpilih = kualitas,
            padaPilih = viewModel::pilihKualitas,
            padaTutup = viewModel::tutupDialogKualitas
        )
    }

    if (tampilKecepatan) {
        DialogKecepatan(
            kecepatanTerpilih = kecepatan,
            padaPilih = viewModel::pilihKecepatan,
            padaTutup = viewModel::tutupDialogKecepatan
        )
    }
}
