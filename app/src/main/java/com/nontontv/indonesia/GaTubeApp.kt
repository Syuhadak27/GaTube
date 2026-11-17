package com.nontontv.indonesia

import android.app.Application
import com.nontontv.indonesia.data.remote.youtube.YouTubeRepository
import dagger.hilt.android.HiltAndroidApp

/** Titik masuk aplikasi; menginisialisasi NewPipeExtractor sekali saja. */
@HiltAndroidApp
class GaTubeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        YouTubeRepository.init(this)
    }
}
