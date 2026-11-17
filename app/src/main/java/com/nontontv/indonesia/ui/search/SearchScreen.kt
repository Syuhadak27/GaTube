package com.nontontv.indonesia.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nontontv.indonesia.ui.components.IndikatorMemuat
import com.nontontv.indonesia.ui.components.KartuVideo
import com.nontontv.indonesia.ui.components.KeadaanKosong
import com.nontontv.indonesia.ui.components.PesanKesalahan
import com.nontontv.indonesia.ui.home.KeadaanMuat

/** Layar pencarian dengan saran dan riwayat pencarian. */
@Composable
fun SearchScreen(onBukaVideo: (String) -> Unit) {
    val viewModel: SearchViewModel = viewModel()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val hasil by viewModel.hasil.collectAsStateWithLifecycle()
    val saran by viewModel.saran.collectAsStateWithLifecycle()
    val riwayat by viewModel.riwayatPencarian.collectAsStateWithLifecycle()
    val keadaan by viewModel.keadaan.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::ubahQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            placeholder = { Text("Cari video...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.ubahQuery("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Hapus")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSearch = { viewModel.cari() }
            )
        )

        when {
            query.isBlank() -> DaftarRiwayatPencarian(
                riwayat = riwayat,
                onPilih = viewModel::pilihRiwayat,
                onHapus = viewModel::hapusRiwayat,
                onBersihkan = viewModel::bersihkanRiwayat
            )

            saran.isNotEmpty() && hasil.isEmpty() -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                items(saran, key = { it }) { saranItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.cariDariSaran(saranItem) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(text = saranItem, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            else -> when (val k = keadaan) {
                KeadaanMuat.Memuat -> IndikatorMemuat("Mencari video...")
                is KeadaanMuat.Gagal -> PesanKesalahan(
                    pesan = k.pesan,
                    onCobaLagi = viewModel::cari
                )
                KeadaanMuat.Berhasil -> {
                    if (hasil.isEmpty()) {
                        KeadaanKosong("Tidak ada hasil untuk \"$query\".")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(hasil, key = { it.id }) { video ->
                                KartuVideo(video = video, onClick = { onBukaVideo(video.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DaftarRiwayatPencarian(
    riwayat: List<com.nontontv.indonesia.data.local.SearchHistoryEntity>,
    onPilih: (String) -> Unit,
    onHapus: (String) -> Unit,
    onBersihkan: () -> Unit
) {
    if (riwayat.isEmpty()) {
        KeadaanKosong("Belum ada riwayat pencarian.")
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Riwayat Pencarian",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onBersihkan) { Text("Bersihkan") }
        }
        LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp)) {
            items(riwayat, key = { it.query }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPilih(item.query) }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = item.query,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onHapus(item.query) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Hapus")
                    }
                }
            }
        }
    }
}
