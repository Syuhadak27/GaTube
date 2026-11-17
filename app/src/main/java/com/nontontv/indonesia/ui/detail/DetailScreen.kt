package com.nontontv.indonesia.ui.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nontontv.indonesia.data.model.CommentItem
import com.nontontv.indonesia.ui.components.IndikatorMemuat
import com.nontontv.indonesia.ui.components.KartuVideo
import com.nontontv.indonesia.ui.components.PesanKesalahan
import com.nontontv.indonesia.ui.home.KeadaanMuat
import com.nontontv.indonesia.util.FormatUtils

/** Layar detail video: info, komentar, dan video terkait. */
@Composable
fun DetailScreen(
    videoId: String,
    onBukaPemain: (String) -> Unit,
    onBukaKanal: (String) -> Unit
) {
    val viewModel: DetailViewModel = viewModel()
    LaunchedEffect(videoId) { viewModel.muat(videoId) }

    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val komentar by viewModel.komentar.collectAsStateWithLifecycle()
    val favorit by viewModel.favorit.collectAsStateWithLifecycle()
    val keadaan by viewModel.keadaan.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when (val k = keadaan) {
        KeadaanMuat.Memuat -> IndikatorMemuat("Memuat detail video...")
        is KeadaanMuat.Gagal -> PesanKesalahan(
            pesan = k.pesan,
            onCobaLagi = { viewModel.muatUlang(videoId) }
        )
        KeadaanMuat.Berhasil -> {
            val d = detail ?: return
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Box {
                        AsyncImage(
                            model = d.thumbnailUrl,
                            contentDescription = d.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .background(Color(0xFF222222))
                        )
                        IconButton(
                            onClick = { onBukaPemain(d.id) },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(72.dp)
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = "Putar",
                                tint = Color.White,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = d.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${FormatUtils.formatJumlah(d.viewCount)} x ditonton" +
                                (if (d.uploadDate.isNotBlank()) " • ${d.uploadDate}" else ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (d.uploaderUrl.isNotBlank()) onBukaKanal(d.uploaderUrl)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = d.uploaderAvatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF333333))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = d.uploaderName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (d.uploaderSubscriberCount > 0) {
                                Text(
                                    text = "${FormatUtils.formatJumlah(d.uploaderSubscriberCount)} pelanggan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = { onBukaPemain(d.id) }) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Putar")
                        }
                        IconButton(onClick = viewModel::beralihFavorit) {
                            Icon(
                                imageVector = if (favorit) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (favorit) "Hapus Favorit" else "Favorit",
                                tint = if (favorit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=${d.id}")
                            }
                            context.startActivity(Intent.createChooser(intent, "Bagikan Video"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Bagikan")
                        }
                    }
                }

                if (d.description.isNotBlank()) {
                    item {
                        Text(
                            text = FormatUtils.potongTeks(d.description, 400),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                item {
                    Text(
                        text = "Komentar (${komentar.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                items(komentar, key = { it.id }) { komentarItem ->
                    BarisKomentar(komentarItem)
                }

                item {
                    Text(
                        text = "Video Terkait",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                items(d.related, key = { it.id }) { terkait ->
                    KartuVideo(
                        video = terkait,
                        onClick = { onBukaPemain(terkait.id) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun BarisKomentar(komentar: CommentItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        AsyncImage(
            model = komentar.authorAvatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF333333))
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = komentar.author,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (komentar.isPinned) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Disematkan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(6.dp))
                if (komentar.publishedTime.isNotBlank()) {
                    Text(
                        text = komentar.publishedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = komentar.text,
                style = MaterialTheme.typography.bodyMedium
            )
            if (komentar.likeCount > 0) {
                Text(
                    text = "${FormatUtils.formatJumlah(komentar.likeCount.toLong)} suka",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
