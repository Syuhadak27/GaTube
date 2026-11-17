package com.nontontv.indonesia.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nontontv.indonesia.ui.components.BarisVideo
import com.nontontv.indonesia.ui.components.IndikatorMemuat
import com.nontontv.indonesia.ui.components.KeadaanKosong
import com.nontontv.indonesia.ui.components.PesanKesalahan
import com.nontontv.indonesia.ui.home.KeadaanMuat
import com.nontontv.indonesia.util.FormatUtils

/** Layar playlist YouTube. */
@Composable
fun PlaylistScreen(
    url: String,
    onBukaVideo: (String) -> Unit
) {
    val viewModel: PlaylistViewModel = viewModel()
    LaunchedEffect(url) { viewModel.muat(url) }

    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val keadaan by viewModel.keadaan.collectAsStateWithLifecycle()

    when (val k = keadaan) {
        KeadaanMuat.Memuat -> IndikatorMemuat("Memuat playlist...")
        is KeadaanMuat.Gagal -> PesanKesalahan(
            pesan = k.pesan,
            onCobaLagi = { viewModel.muatUlang(url) }
        )
        KeadaanMuat.Berhasil -> {
            val d = detail ?: return
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = d.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(160.dp)
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF222222))
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = d.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${FormatUtils.formatJumlah(d.videoCount)} video",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (d.uploaderName.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = d.uploaderName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                if (d.videos.isEmpty()) {
                    item { KeadaanKosong("Playlist ini kosong.") }
                } else {
                    items(d.videos, key = { it.id }) { video ->
                        BarisVideo(
                            video = video,
                            onClick = { onBukaVideo(video.id) },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
