package com.nontontv.indonesia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palet warna GaTube: gelap seperti YouTube, aksen merah khas.
private val MerahGaTube = Color(0xFFFF2D2D)
private val MerahGelap = Color(0xFFCC1F1F)
private val LatarGelap = Color(0xFF0F0F0F)
private val SurfaceGelap = Color(0xFF1F1F1F)
private val SurfaceVariantGelap = Color(0xFF2A2A2A)

private val SkemaGelap = darkColorScheme(
    primary = MerahGaTube,
    onPrimary = Color.White,
    primaryContainer = MerahGelap,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF9E9E9E),
    onSecondary = Color.Black,
    background = LatarGelap,
    onBackground = Color.White,
    surface = SurfaceGelap,
    onSurface = Color.White,
    surfaceVariant = SurfaceVariantGelap,
    onSurfaceVariant = Color(0xFFB3B3B3),
    error = Color(0xFFFF5252),
    onError = Color.Black
)

private val SkemaTerang = lightColorScheme(
    primary = MerahGaTube,
    onPrimary = Color.White,
    background = Color(0xFFF9F9F9),
    onBackground = Color(0xFF111111),
    surface = Color.White,
    onSurface = Color(0xFF111111)
)

/** Tema GaTube. Bawaan selalu gelap agar mirip aplikasi YouTube. */
@Composable
fun GaTubeTheme(
    gelap: Boolean = true,
    konten: @Composable () -> Unit
) {
    val skema = if (gelap || isSystemInDarkTheme()) SkemaGelap else SkemaTerang
    MaterialTheme(
        colorScheme = skema,
        typography = MaterialTheme.typography,
        konten = konten
    )
}
