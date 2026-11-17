package com.nontontv.indonesia.ui.navigasi

import android.net.Uri

/** Kumpulan rute navigasi aplikasi. */
object Rute {
    const val BERANDA = "beranda"
    const val CARI = "cari"
    const val PUSTAKA = "pustaka"
    const val PENGATURAN = "pengaturan"

    const val PEMAIN = "pemain/{videoId}"
    const val DETAIL = "detail/{videoId}"
    const val KANAL = "kanal/{url}"
    const val PLAYLIST = "playlist/{url}"
    const val PAIRING = "pairing"
    const val TAUTAN = "tautan"

    fun pemain(videoId: String) = "pemain/$videoId"
    fun detail(videoId: String) = "detail/$videoId"
    fun kanal(url: String) = "kanal/${Uri.encode(url)}"
    fun playlist(url: String) = "playlist/${Uri.encode(url)}"
}
