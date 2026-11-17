package com.nontontv.indonesia.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Layar "Tautkan Perangkat Baru": perangkat yang sudah login memasukkan kode.
 */
@Composable
fun TautkanPerangkatScreen(
    padaKembali: () -> Unit
) {
    val viewModel: TautkanPerangkatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val kode by viewModel.kode.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Tautkan Perangkat Baru",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Masukkan kode 8 karakter yang ditampilkan di layar " +
                "Masuk dengan Kode, contoh: ABCD-1234.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = kode,
            onValueChange = viewModel::ubahKode,
            label = { Text("Kode Perangkat") },
            placeholder = { Text("ABCD-1234") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        when (val s = status) {
            StatusTautan.Berhasil -> {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Perangkat berhasil ditautkan!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            is StatusTautan.Gagal -> {
                Text(
                    text = s.pesan,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
            }
            StatusTautan.Memproses -> {
                Text(
                    text = "Menyambungkan...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
            }
            StatusTautan.Idle -> {}
        }

        Button(
            onClick = viewModel::setujui,
            enabled = status != StatusTautan.Memproses,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Tautkan Sekarang", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(12.dp))

        androidx.compose.material3.OutlinedButton(
            onClick = padaKembali,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Kembali")
        }
    }
}
