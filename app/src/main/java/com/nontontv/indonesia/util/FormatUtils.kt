package com.nontontv.indonesia.util

import java.util.Locale
import java.util.regex.Pattern

/**
 * Kumpulan fungsi bantu untuk memformat angka, durasi, dan URL YouTube.
 * Semua teks keluaran memakai format Indonesia.
 */
object FormatUtils {

    /** Contoh: 980 -> "980", 12_345 -> "12,3 rb", 1_250_000 -> "1,3 jt". */
    fun formatJumlah(nilai: Long): String {
        return when {
            nilai < 0 -> "-"
            nilai < 1_000 -> nilai.toString()
            nilai < 1_000_000 -> {
                val rb = nilai / 1_000.0
                String.format(Locale("id", "ID"), "%.1f rb", rb).removeTrailingZero()
            }
            nilai < 1_000_000_000 -> {
                val jt = nilai / 1_000_000.0
                String.format(Locale("id", "ID"), "%.1f jt", jt).removeTrailingZero()
            }
            else -> {
                val m = nilai / 1_000_000_000.0
                String.format(Locale("id", "ID"), "%.1f M", m).removeTrailingZero()
            }
        }
    }

    /** Contoh: 65 -> "1:05", 3725 -> "1:02:05". */
    fun formatDurasi(detik: Long): String {
        if (detik <= 0) return "0:00"
        val jam = detik / 3600
        val menit = (detik % 3600) / 60
        val detikSisa = detik % 60
        return if (jam > 0) {
            String.format(Locale.US, "%d:%02d:%02d", jam, menit, detikSisa)
        } else {
            String.format(Locale.US, "%d:%02d", menit, detikSisa)
        }
    }

    /** Menghapus ",0" yang berlebihan supaya "12,0 rb" menjadi "12 rb". */
    private fun String.removeTrailingZero(): String {
        return if (endsWith(",0")) dropLast(2) else this
    }

    /** Menonaktifkan teks yang terlalu panjang di komentar. */
    fun potongTeks(teks: String, maksimum: Int = 160): String {
        return if (teks.length <= maksimum) teks else teks.take(maksimum).trimEnd() + "..."
    }

    /**
     * Mengambil ID video dari berbagai bentuk URL YouTube.
     * Mengembalikan null bila URL bukan video.
     */
    fun ekstrakIdVideo(url: String): String? {
        if (url.isBlank()) return null
        val pola = Pattern.compile(
            "(?:youtube\\.com/(?:watch\\?.*v=|shorts/|live/|embed/|v/)|youtu\\.be/)([a-zA-Z0-9_-]{11})"
        )
        val pencocok = pola.matcher(url)
        return if (pencocok.find()) pencocok.group(1) else null
    }

    /** Mengambil ID video atau mengembalikan teks asli bila bukan URL YouTube. */
    fun ekstrakIdVideoAtauTeks(urlAtauId: String): String {
        return ekstrakIdVideo(urlAtauId) ?: urlAtauId
    }
}
