package com.nontontv.indonesia

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nontontv.indonesia.data.local.SettingsRepository
import com.nontontv.indonesia.playback.PlaybackController
import com.nontontv.indonesia.ui.login.LoginViewModel
import com.nontontv.indonesia.ui.login.TampilanLogin
import com.nontontv.indonesia.ui.navigasi.TampilanUtama
import com.nontontv.indonesia.ui.theme.GaTubeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Satu-satunya Activity; menangani PiP, izin notifikasi, dan navigasi. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var playbackController: PlaybackController

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private var pipOtomatisAktif by mutableStateOf(true)

    private val permintaanIzinNotifikasi =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* abaikan */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33) {
            val izin = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            if (izin != PackageManager.PERMISSION_GRANTED) {
                permintaanIzinNotifikasi.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            GaTubeTheme {
                PermukaanUtama()
            }
        }
    }

    /** Masuk Picture-in-Picture saat pengguna menekan tombol Home. */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (pipOtomatisAktif && playbackController.player.isPlaying) {
            masukPip()
        }
    }

    /** Tetap jalankan playback saat layar mati / mode PiP. */
    private fun masukPip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = android.app.PictureInPictureParams.Builder()
                .setAspectRatio(android.util.Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        } else {
            @Suppress("DEPRECATION")
            enterPictureInPictureMode()
        }
    }

    @Composable
    private fun PermukaanUtama() {
        val loginViewModel: LoginViewModel = viewModel()
        val sudahLihatLogin by loginViewModel.sudahLihatLogin.collectAsStateWithLifecycle(initialValue = null)
        val authState = sudahLihatLogin
        val pipOtomatis by settingsRepository.pipOtomatis.collectAsStateWithLifecycle(initialValue = true)

        LaunchedEffect(pipOtomatis) { pipOtomatisAktif = pipOtomatis }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (authState) {
                null -> LayarMemuat()
                false -> TampilanLogin(
                    onLewati = { loginViewModel.tandaiSudahLihatLogin() },
                    onMasukKode = { loginViewModel.mulaiPairing() },
                    pairingTersedia = loginViewModel.pairingTersedia.collectAsStateWithLifecycle().value
                )
                true -> TampilanUtama()
            }
        }
    }

    @Composable
    private fun LayarMemuat() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "GaTube",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
