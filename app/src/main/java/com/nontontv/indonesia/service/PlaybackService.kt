package com.nontontv.indonesia.service

import android.content.Intent
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.nontontv.indonesia.playback.PlaybackController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Service latar untuk background play + notifikasi media.
 * Media3 otomatis membuat notifikasi dengan tombol Putar/Jeda, Berikutnya,
 * Sebelumnya, dan Tutup selama sesi media aktif.
 */
@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var playbackController: PlaybackController

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val session = MediaSession.Builder(this, playbackController.player).build()
        mediaSession = session
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = playbackController.player
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
