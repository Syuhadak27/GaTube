package com.nontontv.indonesia.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Layar masuk pertama kali: pilih "Masuk dengan Kode" atau "Lewati / Mode Tamu".
 */
@Composable
fun TampilanLogin(
    onLewati: () -> Unit,
    onMasukKode: () -> Unit,
    pairingTersedia: Boolean
) {
    val loginViewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val keadaan by loginViewModel.keadaan.collectAsStateWithLifecycle()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { dalam ->
        Box(modifier = Modifier.fillMaxSize().padding(dalam)) {
            when (val state = keadaan) {
                is StatusLogin.Pairing -> LayarPairing(
                    kode = state.kode,
                    padaBuatUlang = loginViewModel::buatUlangKode,
                    padaKembali = loginViewModel::keluarDariPairing
                )
                StatusLogin.Pilih -> PilihanMasuk(
                    pairingTersedia = pairingTersedia,
                    onLewati = onLewati,
                    onMasukKode = onMasukKode
                )
            }
        }
    }
}

@Composable
private fun PilihanMasuk(
    pairingTersedia: Boolean,
    onLewati: () -> Unit,
    onMasukKode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Tv,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "GaTube",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "YouTube tanpa iklan. Bisa Picture-in-Picture dan background play.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onMasukKode,
            enabled = pairingTersedia,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Masuk dengan Kode", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLewati,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Lewati / Mode Tamu", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = "Sebagai tamu, kamu tetap bisa menonton. Riwayat, favorit, dan " +
                "sesi login tersimpan hanya di perangkat ini.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
