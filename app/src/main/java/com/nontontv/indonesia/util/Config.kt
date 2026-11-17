package com.nontontv.indonesia.util

/**
 * Pusat konfigurasi GaTube.
 *
 * Nama pengguna dan repo GitHub sudah diisi sesuai repo pribadi Anda.
 * Ganti bila nama repo berubah.
 */
object Config {
    const val GITHUB_USERNAME = "Syuhadak27"
    const val GITHUB_REPO = "GaTube"

    /** URL API GitHub untuk release terbaru. */
    const val GITHUB_RELEASE_API =
        "https://api.github.com/repos/$GITHUB_USERNAME/$GITHUB_REPO/releases/latest"

    /** Durasi berlaku kode pairing (detik). */
    const val PAIRING_EXPIRED_DETIK: Long = 10 * 60L

    /** URL dasar YouTube. */
    const val YOUTUBE_URL = "https://www.youtube.com"

    /** Kualitas video bawaan saat pertama kali memutar. */
    const val KUALITAS_BAWAAN = "720p"
}
