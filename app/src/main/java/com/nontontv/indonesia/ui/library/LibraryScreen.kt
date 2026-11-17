package com.nontontv.indonesia.ui.library

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nontontv.indonesia.data.local.FavoriteEntity
import com.nontontv.indonesia.data.local.HistoryEntity
import com.nontontv.indonesia.data.model.VideoItem
import com.nontontv.indonesia.ui.components.BarisVideo
import com.nontontv.indonesia.ui.components.KeadaanKosong

/** Layar pustaka: Riwayat tontonan dan Favorit. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(onBukaVideo: (String) -> Unit) {
    val viewModel: LibraryViewModel = viewModel()
    val riwayat by viewModel.riwayat.collectAsStateWithLifecycle()
    val favorit by viewModel.favorit.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "Pustaka",
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        TabRow(selectedTabIndex = tab) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0 },
                text = { Text("Riwayat") },
                icon = { Icon(Icons.Filled.History, contentDescription = null) }
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1 },
                text = { Text("Favorit") },
                icon = { Icon(Icons.Filled.Favorite, contentDescription = null) }
            )
        }

        if (tab == 0) {
            if (riwayat.isEmpty()) {
                KeadaanKosong("Belum ada riwayat tontonan.")
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${riwayat.size} video",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = viewModel::bersihkanRiwayat) {
                                Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Bersihkan")
                            }
                        }
                    }
                    items(riwayat, key = { it.videoId }) { item ->
                        BarisPustaka(
                            video = item.toVideoItem(),
                            onClick = { onBukaVideo(item.videoId) },
                            onHapus = { viewModel.hapusRiwayat(item.videoId) }
                        )
                    }
                }
            }
        } else {
            if (favorit.isEmpty()) {
                KeadaanKosong("Belum ada video favorit.")
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(favorit, key = { it.videoId }) { item ->
                        BarisPustaka(
                            video = item.toVideoItem(),
                            onClick = { onBukaVideo(item.videoId) },
                            onHapus = { viewModel.hapusFavorit(item.videoId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarisPustaka(
    video: VideoItem,
    onClick: () -> Unit,
    onHapus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BarisVideo(
            video = video,
            onClick = onClick,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onHapus) {
            Icon(Icons.Filled.Delete, contentDescription = "Hapus")
        }
    }
}

private fun HistoryEntity.toVideoItem() = VideoItem(
    id = videoId,
    title = title,
    thumbnailUrl = thumbnailUrl,
    durationSeconds = durationSeconds,
    viewCount = viewCount,
    uploaderName = uploaderName,
    uploaderUrl = uploaderUrl,
    uploaderAvatarUrl = uploaderAvatarUrl,
    uploadDate = uploadDate
)

private fun FavoriteEntity.toVideoItem() = VideoItem(
    id = videoId,
    title = title,
    thumbnailUrl = thumbnailUrl,
    durationSeconds = durationSeconds,
    viewCount = viewCount,
    uploaderName = uploaderName,
    uploaderUrl = uploaderUrl,
    uploaderAvatarUrl = uploaderAvatarUrl,
    uploadDate = uploadDate
)
