package com.nontontv.indonesia.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nontontv.indonesia.ui.components.IndikatorMemuat
import com.nontontv.indonesia.ui.components.KeadaanKosong
import com.nontontv.indonesia.ui.components.KartuVideo
import com.nontontv.indonesia.ui.components.PesanKesalahan

/** Layar beranda: kategori + daftar video trending/kategori. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onBukaVideo: (String) -> Unit) {
    val viewModel: HomeViewModel = viewModel()
    val video by viewModel.video.collectAsStateWithLifecycle()
    val kategori by viewModel.kategori.collectAsStateWithLifecycle()
    val kategoriTerpilih by viewModel.kategoriTerpilih.collectAsStateWithLifecycle()
    val keadaan by viewModel.keadaan.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "GaTube",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        if (kategori.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(kategori, key = { it.id }) { kategoriItem ->
                    FilterChip(
                        selected = kategoriItem.id == kategoriTerpilih,
                        onClick = { viewModel.pilihKategori(kategoriItem.id) },
                        label = { Text(kategoriItem.nama) }
                    )
                }
            }
        }

        when (val k = keadaan) {
            KeadaanMuat.Memuat -> IndikatorMemuat("Memuat video...")
            is KeadaanMuat.Gagal -> PesanKesalahan(
                pesan = k.pesan,
                onCobaLagi = viewModel::muatUlang
            )
            KeadaanMuat.Berhasil -> {
                if (video.isEmpty()) {
                    KeadaanKosong("Belum ada video untuk kategori ini.")
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(video, key = { it.id }) { videoItem ->
                            KartuVideo(video = videoItem, onClick = { onBukaVideo(videoItem.id) })
                        }
                    }
                }
            }
        }
    }
}
