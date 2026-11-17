package com.nontontv.indonesia.ui.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

/** Layar kanal: info kanal + daftar video. */
@Composable
fun ChannelScreen(
    url: String,
    onBukaVideo: (String) -> Unit
) {
    val viewModel: ChannelViewModel = viewModel()
    LaunchedEffect(url) { viewModel.muat(url) }

    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val keadaan by viewModel.keadaan.collectAsStateWithLifecycle()

    when (val k = keadaan) {
        KeadaanMuat.Memuat -> IndikatorMemuat("Memuat kanal...")
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
                    Column {
                        if (d.bannerUrl.isNotBlank()) {
                            AsyncImage(
                                model = d.bannerUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(Color(0xFF222222))
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = d.avatarUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF333333))
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = d.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${FormatUtils.formatJumlah(d.subscriberCount)} pelanggan" +
                                        " • ${FormatUtils.formatJumlah(d.videoCount)} video",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (d.description.isNotBlank()) {
                            Text(
                                text = FormatUtils.potongTeks(d.description, 300),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (d.videos.isEmpty()) {
                    item { KeadaanKosong("Belum ada video di kanal ini.") }
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
